package gr.server.impl.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.common.auth.JwtUtils;
import gr.server.common.bean.AuthorizationBean;
import gr.server.common.bean.PurchaseVerificationResponseBean;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.dto.LoginResponseDto;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.league.Season;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPurchase;
import gr.server.def.service.MyBetOddsService;
import gr.server.email.EmailSendUtil;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.mongo.bean.PlaceBetResponseBean;
import gr.server.util.SecureUtils;

@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl implements MyBetOddsService {

	@Override
	@POST
	@Path("/verifyPurchase")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response verifyPurchase(String verificationString) {
		UserPurchase verificationBean = new Gson().fromJson(verificationString, new TypeToken<UserPurchase>() {}.getType());

		boolean verifiedPurchase = true;//TODO
		boolean applied = false;
		if (verifiedPurchase) {
			applied = new MongoClientHelperImpl().storePurchase(verificationBean);
		}

		return Response.ok(new Gson().toJson(new PurchaseVerificationResponseBean(applied))).build();

	}

	
	@Override
	@POST
	@Path("/placeBet")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response placeBet(String userBetJson) {
		UserBet newBet = new Gson().fromJson(userBetJson, new TypeToken<UserBet>() {}.getType());
		PlaceBetResponseBean betPlacementResp = new MongoClientHelperImpl().placeBet(newBet);
		return Response.ok(new Gson().toJson(betPlacementResp)).build();
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
		User incomingUser = new Gson().fromJson(userJson, new TypeToken<User>() {
		}.getType());
		String userEmail = SecureUtils.decode(incomingUser.getEmail().trim());
		LoginResponseDto loginResponse = new MongoClientHelperImpl().loginUser(incomingUser.getUsername(), userEmail,
				incomingUser.getPassword());

		User responseUser = null;
		if (loginResponse.getMongoId() != null) {
			responseUser = getUserFromMongoId(loginResponse.getMongoId());
			System.out.println("RETURNING " + responseUser);
		}

		return Response.ok(new Gson().toJson(responseUser)).build();
	}

	@Override
	@POST
	@Path("/authorize")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response authorize(String uniqueDeviceId) throws Exception {
		Gson gson = new Gson();
		AuthorizationBean authBean = gson.fromJson(uniqueDeviceId, AuthorizationBean.class);
		String decodedString = SecureUtils.validateDeviceId(authBean);
		String generateToken = JwtUtils.generateToken(decodedString);
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setAccessToken(generateToken);

		return Response.ok(gson.toJson(tokenResponse)).build();
	}

	@Override
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/{email}/validateUser")
	public String validateUser(@PathParam("email") String email) throws Exception {
		new MongoClientHelperImpl().validateUser(email);
		return "<html><head><body>Your email is validated. Please return to the app and Login again.</body></head></html>";
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getUser/{id}")
	public Response getUser(@PathParam("id") String id) {
		User user = getUserFromMongoId(id);
		String userJson = new Gson().toJson(user);
		return Response.ok(userJson).build();
	}

//
//	@Override
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("getLiveSpecific/{ids}")
//	public Response getLiveSpecific(@PathParam("ids") String ids) {
//		Set<MatchEvent> liveEvents = new MongoClientHelperImpl().getLiveByIds(ids);
//		String eventsJson = new Gson().toJson(liveEvents);
//		return Response.ok(eventsJson).build();
//	}
//	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getSections")
	public Response getSections() {
		return Response
				.ok(new Gson().toJson(FootballApiCache.ALL_SECTIONS.values().stream().collect(Collectors.toList())))
				.build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public Response getLeagues() {
		return Response
				.ok(new Gson().toJson(FootballApiCache.ALL_LEAGUES.values().stream().collect(Collectors.toList())))
				.build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagueEvents")
	public Response getLeagueEvents() {
		for(MatchEvent e: FootballApiCache.ALL_EVENTS.values()) {
			if (e.getHome_team().getName().contains("Leipzig") || e.getHome_team().getName().contains("CD Vitoria")
//					||  e.getHome_team().getName().contains("rentina")
					||  e.getHome_team().getName().contains("Savoia 1908")
					||  e.getHome_team().getName().contains("SSD Pro")
					||  e.getHome_team().getName().contains("Ivory Coast")) {
				System.out.println("EVENT:::::::" + e);
			}
		}

		return Response.ok(new Gson().toJson(FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY)).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLiveEvents")
	public Response getLiveEvents() {
		for (MatchEvent e : FootballApiCache.LIVE_EVENTS.values()) {
			if (e.getHome_team().getName().contains("AEL L")) {
				System.out.println(e);
			}
		}

		return Response.ok(new Gson().toJson(FootballApiCache.LIVE_EVENTS)).build();
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
		return Response.ok(new Gson().toJson(FootballApiCache.LEADERS)).build();
	}

	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getEventStatistics/{id}")
	public Response getEventStatistics(@PathParam("id") Integer id) {
		MatchEventIncidentsWithStatistics stats = FootballApiCache.ALL_MATCH_STATS.get(id);
		return Response.ok(new Gson().toJson(stats)).build();
	}

	/**
	 * @param mongoId
	 * @return
	 */
	User getUserFromMongoId(String mongoId) {
		long fiveDaysBefore = 5 * 1000 * 24 * 60 * 60;
		User user = new MongoClientHelperImpl().getUser(mongoId, 20, fiveDaysBefore, true, true, true, true);
		return user;
	}

}
