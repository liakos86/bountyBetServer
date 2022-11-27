package gr.server.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.application.RestApplication;
import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.def.service.MyBetOddsService;
import gr.server.email.EmailSendUtil;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.util.DateUtils;
import gr.server.util.SecureUtils;


@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl 
implements MyBetOddsService {

	
	@Override
	@GET
    @Path("/{id}/myOpenBets")
	public List<UserBet> getMyOpenBets(@PathParam("id") String id) {
		return new MongoClientHelperImpl().getBetsForUser(id);
	}
	
	@Override
	@POST
    @Path("/placeBet")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response placeBet(String userBetJson) {
		UserBet newBet =  new Gson().fromJson(userBetJson,
				new TypeToken<UserBet>() {}.getType());
		
		/**
		 * TODO: validations
		 */
		
		newBet.setBetStatus(BetStatus.PENDING);
		for (UserPrediction betPrediction: newBet.getPredictions()) {
			betPrediction.setPredictionStatus(PredictionStatus.PENDING);
		}
		
		newBet = new MongoClientHelperImpl().placeBet(newBet);
		
		return Response.ok(new Gson().toJson(newBet)).build();		
	}
	
	@Override
	@POST
    @Path("/registerUser")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response registerUser(String userJson) throws Exception{
		User newUser = new Gson().fromJson(userJson, new TypeToken<User>() {}.getType());
		String userEmail = SecureUtils.decode(newUser.getEmail());
		newUser.setEmail(userEmail);
		newUser = new MongoClientHelperImpl().createUser(newUser);
		if (newUser.getErrorMessage() == null && newUser.getMongoId() != null) {
			EmailSendUtil.doSend(userEmail);
		}
		return Response.ok(new Gson().toJson(newUser)).build();	
	}
	
	@Override
	@GET
	@Produces( MediaType.TEXT_HTML)
	@Path("{email}/validateUser")
	public String validateUser(@PathParam("email") String email) throws Exception {
		new MongoClientHelperImpl().validateUser(email);
		return "<html><head><body>Your email is validated. Please return to the app and Login again.</body></head></html>";	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getUser/{id}")
	public Response getUser(@PathParam("id") String id) {
		return Response.ok(new Gson().toJson(new MongoClientHelperImpl().getUser(id))).build();	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public Response getLeagues(){
		List<League> todayLeagues = new ArrayList<>();
		
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = RestApplication.EVENTS_PER_DAY_PER_LEAGUE.get(DateUtils.todayStr());
		for (Map.Entry<League, Map<Integer, MatchEvent>> entry : todayLeaguesWithEvents.entrySet()) {
			League league = entry.getKey();
			if (league == null) {
				continue;
			}
			
			if (league.getSection_id() > 0) {
				league.setSection(RestApplication.SECTIONS.get(league.getSection_id()));
			}
			
			Map<Integer, MatchEvent> eventsOfLeagueMap = entry.getValue();
			List<MatchEvent> events = new ArrayList<>(eventsOfLeagueMap.values());
			league.setLiveMatchEvents(events);
			events.forEach(e->e.setLeague(null));
			Collections.sort(league.getLiveMatchEvents());
			todayLeagues.add(league);
		}
		
		Collections.sort(todayLeagues);
		
		return  Response.ok(new Gson().toJson(todayLeagues)).build();
	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLive")
	public String getLive(){
		List<League> leagues = new ArrayList<>();
		for (Entry<League, Map<Integer, MatchEvent>> entry : new HashMap<>(RestApplication.LIVE_EVENTS_PER_LEAGUE).entrySet()) {
			League league = entry.getKey();
			if (league == null) {
				RestApplication.LIVE_EVENTS_PER_LEAGUE.remove(league);
				continue;
			}
			
			
			Map<Integer, MatchEvent> eventsOfLeagueMap = entry.getValue();
			if (eventsOfLeagueMap == null || eventsOfLeagueMap.isEmpty()) {
				continue;
			}
			
			List<MatchEvent> events = new ArrayList<>(eventsOfLeagueMap.values());
			league.setLiveMatchEvents(events);
			if (league.getSection_id() > 0) {
				league.setSection(RestApplication.SECTIONS.get(league.getSection_id()));
			}
			events.forEach(e->fixEvent(e));
			Collections.sort(events);
			leagues.add(league);
		}
		
		Collections.sort(leagues);
		
		return  new Gson().toJson(leagues);
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLiveUpdates")
	public String getLiveUpdates(){
		System.out.println("SERVING GOALS");
		return  new Gson().toJson(RestApplication.LIVE_CHANGES_PER_EVENT);
	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeaderBoard")
	public String getLeaderBoard(){
		return  new Gson().toJson(new MongoClientHelperImpl().retrieveLeaderBoard()); 
	
	}

	private void fixEvent(MatchEvent e) {
		e.setLeague(null);
		if (e.getStatus_for_client() == null) {
			e.setStatus_for_client(e.getStatus());
		}
	}

}
