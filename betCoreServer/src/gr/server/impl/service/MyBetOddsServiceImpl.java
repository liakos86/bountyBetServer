package gr.server.impl.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.events.MatchEventStatistics;
import gr.server.data.api.model.league.Season;
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.def.service.MyBetOddsService;
import gr.server.email.EmailSendUtil;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.util.SecureUtils;

@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl implements MyBetOddsService {

	@Override
	@POST
	@Path("/placeBet")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response placeBet(String userBetJson) {
		UserBet newBet = new Gson().fromJson(userBetJson, new TypeToken<UserBet>() {
		}.getType());

		BetPlacementStatus betPlacementStatus = new MongoClientHelperImpl().placeBet(newBet);

		if (BetPlacementStatus.PLACED != betPlacementStatus) {
			User errorUser = new User();
			errorUser.setErrorMessage(String.valueOf(betPlacementStatus.getCode()));
			return Response.ok(new Gson().toJson(errorUser)).build();
		}

		return getUser(newBet.getMongoUserId());// TODO: maybe return only the bet?

	}

	@Override
	@POST
	@Path("/registerUser")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response registerUser(String userJson) throws Exception {
		User newUser = new Gson().fromJson(userJson, new TypeToken<User>() {
		}.getType());
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
	public Response loginUser(String userJson) throws Exception {
		User newUser = new Gson().fromJson(userJson, new TypeToken<User>() {
		}.getType());
		String userEmail = SecureUtils.decode(newUser.getEmail().trim());
		newUser.setEmail(userEmail);
		newUser = new MongoClientHelperImpl().loginUser(newUser);
		if (newUser.getErrorMessage() == null) {
			newUser = getUserFromMongoId(newUser.getMongoId());
			System.out.println("RETURNING " + newUser.getEmail());
		}
		return Response.ok(new Gson().toJson(newUser)).build();
	}

	@Override
	@GET
	@Produces(MediaType.TEXT_HTML)
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

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getLiveSpecific/{ids}")
	public Response getLiveSpecific(@PathParam("ids") String ids) {
		Set<MatchEvent> liveEvents = new MongoClientHelperImpl().getLiveByIds(ids);
		String eventsJson = new Gson().toJson(liveEvents);
		return Response.ok(eventsJson).build();
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getSections")
	public Response getSections() {
		return Response.ok(new Gson().toJson(FootballApiCache.ALL_SECTIONS.values().stream().collect(Collectors.toList()))).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public Response getLeagues() {
		return Response.ok(new Gson().toJson(FootballApiCache.ALL_LEAGUES.values().stream().collect(Collectors.toList()))).build();
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagueEvents")
	public Response getLeagueEvents() {
		return Response.ok(new Gson().toJson(FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY)).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getStandingsAllWithoutTables")
	public Response getStandingsAllWithoutTables() {
		return Response.ok(new Gson().toJson(FootballApiCache.ALL_LEAGUES.values().stream().collect(Collectors.toList()))).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getStandingsOfSeason/{leagueId}/{seasonId}")
	public Response getStandingsOfSeason(@PathParam("leagueId") Integer leagueId,
			@PathParam("seasonId") Integer seasonId) {
		List<Season> seasonsOfLeague = FootballApiCache.SEASONS_PER_LEAGUE.get(leagueId);
		Optional<Season> seasonOpt = seasonsOfLeague.stream().filter(s -> (s.getId() == seasonId)).findFirst();
		Season season = seasonOpt.isPresent() ? seasonOpt.get() : new Season();
		return Response.ok(new Gson().toJson(season)).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeaderBoard")
	public Response getLeaderBoard() {
		return Response.ok(new Gson().toJson(new MongoClientHelperImpl().retrieveLeaderBoard())).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getEventStatistics/{id}")
	public Response getEventStatistics(@PathParam("id") Integer id) {
		return Response.ok(FootballApiCache.STATS_PER_EVENT.getOrDefault(id, new MatchEventStatistics())).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getEventIncidents/{id}")
	public Response getEventIncidents(@PathParam("id") Integer id) {
		return Response.ok(FootballApiCache.INCIDENTS_PER_EVENT.getOrDefault(id, new MatchEventIncidents())).build();
	}

	/**
	 * @param mongoId
	 * @return
	 */
	User getUserFromMongoId(String mongoId) {
		User user = new MongoClientHelperImpl().getUser(mongoId);

		return user;
	}

}
