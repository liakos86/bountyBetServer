package gr.server.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Score;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.def.service.MyBetOddsService;
import gr.server.email.EmailSendUtil;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.util.SecureUtils;


@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl 
implements MyBetOddsService {

	
//	@Override
//	@GET
//    @Path("/{id}/myOpenBets")
//	public List<UserBet> getMyOpenBets(@PathParam("id") String id) {
//		return new MongoClientHelperImpl().getBetsForUser(id);
//	}
	
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
		
		new MongoClientHelperImpl().placeBet(newBet);
		
		return getUser(newBet.getMongoUserId());	
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
	@POST
    @Path("/loginUser")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response loginUser(String userJson) throws Exception{
		User newUser = new Gson().fromJson(userJson, new TypeToken<User>() {}.getType());
		String userEmail = SecureUtils.decode(newUser.getEmail());
		newUser.setEmail(userEmail);
		newUser = new MongoClientHelperImpl().loginUser(newUser);
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
		User user = new MongoClientHelperImpl().getUser(id);
		user.getUserBets().forEach(b-> b.getPredictions().forEach(
				p -> {
					MatchEvent event = RestApplication.ALL_EVENTS.get(p.getEventId());
					if (event == null) {
						event = mockEvent();
					}
					
					p.setEvent(event);
				}));
		String userJson = new Gson().toJson(user);
		return Response.ok(userJson).build();
	}
	
	/**
	 * TODO: if a match is not in memory we should call SPORTSCORE API to retrieve it.
	 * 
	 * @return
	 */
	private MatchEvent mockEvent() {
		MatchEvent mockEvent = new MatchEvent();
		mockEvent.setId(-1);
		Team mockTeam = new Team();
		mockTeam.setName("Mockalona");
		mockEvent.setHome_team(mockTeam );
		mockEvent.setAway_team(mockTeam);
		Score mockScore = new Score();
		mockScore.setCurrent(1);
		mockEvent.setHome_score(mockScore );
		mockEvent.setAway_score(mockScore);
		return mockEvent;
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public Response getLeagues(){
		Map<String, List<League>> leaguesPerDay = new LinkedHashMap<>();
		
		for ( Map.Entry<String, Map<League, Map<Integer, MatchEvent>>> dailyEntry : RestApplication.EVENTS_PER_DAY_PER_LEAGUE.entrySet()) {
		List<League> dailyLeagues = new ArrayList<>();
		
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = dailyEntry.getValue();
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
			dailyLeagues.add(league);
		}
		
		Collections.sort(dailyLeagues);
		leaguesPerDay.put(dailyEntry.getKey(), dailyLeagues);
		}
		
		return  Response.ok(new Gson().toJson(leaguesPerDay)).build();
	
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

//	@Override
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/getLiveUpdates")
//	public String getLiveUpdates(){
//		System.out.println("SERVING GOALS");
//		return  new Gson().toJson(RestApplication.LIVE_CHANGES_PER_EVENT);
//	
//	}
	
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
