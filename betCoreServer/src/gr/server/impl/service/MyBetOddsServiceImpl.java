package gr.server.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.def.service.MyBetOddsService;
import gr.server.email.EmailSendUtil;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.util.SecureUtils;


@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl 
implements MyBetOddsService {

	
	@Override
	@POST
    @Path("/placeBet")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response placeBet(String userBetJson) {
		UserBet newBet =  new Gson().fromJson(userBetJson,
				new TypeToken<UserBet>() {}.getType());
		
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
		String userEmail = SecureUtils.decode(newUser.getEmail().trim());
		newUser.setEmail(userEmail);
		newUser = new MongoClientHelperImpl().loginUser(newUser);
		if (newUser.getErrorMessage() == null) {
			newUser = getUserFromMongoId(newUser.getMongoId());
			System.out.println("RETURNING "+newUser.getEmail());
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
		User user = getUserFromMongoId(id);
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
		mockTeam.setLogo("https://tipsscore.com/resb/no-league.png");
		mockEvent.setHome_team(mockTeam );
		mockEvent.setAway_team(mockTeam);
		Score mockScore = new Score();
		mockScore.setCurrent(1);
		mockEvent.setHome_score(mockScore );
		mockEvent.setAway_score(mockScore);
		mockEvent.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		return mockEvent;
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public Response getLeagues(){
		Map<Integer, List<League>> leaguesPerDay = new LinkedHashMap<>();
		
		for ( Map.Entry<Integer, Map<League, Map<Integer, MatchEvent>>> dailyEntry : RestApplication.EVENTS_PER_DAY_PER_LEAGUE.entrySet()) {
			List<League> dailyLeagues = new ArrayList<>();
			
			Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = dailyEntry.getValue();
			for (Map.Entry<League, Map<Integer, MatchEvent>> entry : todayLeaguesWithEvents.entrySet()) {
				League league = entry.getKey();
				if (league.getSection_id() > 0) {
					Section section = RestApplication.SECTIONS.get(league.getSection_id());
					league.setSection(section);
				}
				
				Map<Integer, MatchEvent> eventsOfLeagueMap = entry.getValue();
				List<MatchEvent> events = new ArrayList<>(eventsOfLeagueMap.values());
				league.setMatchEvents(events);
				events.forEach(e->e.setLeague(null));//prevent stackoverflow
				Collections.sort(league.getMatchEvents());
				dailyLeagues.add(league);
				
				Integer priorityOverride = RestApplication.PRIORITIES_OVERRIDDE.get(league.getId());
				if (priorityOverride!=null && priorityOverride > 0) {
					league.setPriority(priorityOverride);
				}else if (league.getPriority() == null) {
					league.setPriority(0);
				}
				
			}
			
			Collections.sort(dailyLeagues);
			
			leaguesPerDay.put(dailyEntry.getKey(), dailyLeagues);
		}
		
		return  Response.ok(new Gson().toJson(leaguesPerDay)).build();
	
	}
	
//	@Override
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/getLive")
//	public String getLive(){
//		List<League> leagues = new ArrayList<>();
//		for (Entry<League, Map<Integer, MatchEvent>> entry : new HashMap<>(RestApplication.LIVE_EVENTS_PER_LEAGUE).entrySet()) {
//			League league = entry.getKey();
//			if (league == null) {
//				RestApplication.LIVE_EVENTS_PER_LEAGUE.remove(league);
//				continue;
//			}
//			
//			
//			Map<Integer, MatchEvent> eventsOfLeagueMap = entry.getValue();
//			if (eventsOfLeagueMap == null || eventsOfLeagueMap.isEmpty()) {
//				continue;
//			}
//			
//			List<MatchEvent> events = new ArrayList<>(eventsOfLeagueMap.values());
//			league.setMatchEvents(events);
//			if (league.getSection_id() > 0) {
//				league.setSection(RestApplication.SECTIONS.get(league.getSection_id()));
//			}
//			events.forEach(e->fixEvent(e));
//			Collections.sort(events);
//			leagues.add(league);
//		}
//		
//		Collections.sort(leagues);
//		
//		return  new Gson().toJson(leagues);
//	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeaderBoard")
	public Response getLeaderBoard(){
		return  Response.ok(new Gson().toJson(new MongoClientHelperImpl().retrieveLeaderBoard())).build(); 
	}

	/**
	 * Every prediction of every bet has an eventId, which is the match that corresponds to it.
	 * We try to find the match in the cache. Obviously it is not always there.
	 * 
	 * TODO: call api.
	 * 
	 * @param mongoId
	 * @return
	 */
	private User getUserFromMongoId(String mongoId) {
		User user = new MongoClientHelperImpl().getUser(mongoId);
		user.getUserBets().forEach(b-> b.getPredictions().forEach(
			p -> {
					/**
					 * TODO: Call api here? or save event info in mongo?
					 */
					MatchEvent event = RestApplication.ALL_EVENTS.get(p.getEventId());
					if (event == null) {
						event = mockEvent();
					}
					
					p.setEvent(event);
				}));
		return user;
	}

}
