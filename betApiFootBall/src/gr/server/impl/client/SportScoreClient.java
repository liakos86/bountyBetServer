package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.events.MatchEventStatistics;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.events.PlayerStatistic;
import gr.server.data.api.model.events.PlayerStatistics;
import gr.server.data.api.model.events.Players;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Seasons;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Sections;
import gr.server.data.api.model.league.StandingTable;
import gr.server.data.api.model.league.StandingTables;
import gr.server.data.api.model.league.StandingTable;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.util.HttpHelper;

public class SportScoreClient {
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
//	public static Teams getTeams() throws IOException, ParseException, InterruptedException, URISyntaxException {
//		System.out.println("GETTING API FOOTBALL TEAMS");
//		String url = SportScoreApiConstants.GET_TEAMS_BY_SPORT_URL;
//
//		/**
//		 * TODO pass the url
//		 */
//		String content = new HttpHelper().fetchGetContentWithHeaders("teams1.json");
//		System.out.println(content);
//		
//		Teams teams= new Gson().fromJson(content, new TypeToken<Teams>() {}.getType());
//		System.out.println(teams.getData().get(0).getName());
//		return teams;
//
//	}
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
	public static Events getEvents(Date date) throws IOException, ParseException, InterruptedException, URISyntaxException {
		String url = SportScoreApiConstants.GET_EVENTS_BY_SPORT_DATE_URL;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.GET_EVENTS_DATE_FORMAT);
		String today = simpleDateFormat.format(date);
		url += today;
		
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		System.out.println("EVENTS: + 0" + content);
		Events events= new Gson().fromJson(content, new TypeToken<Events>() {}.getType());
		MatchEventIncidents matchIncidentsFromFile = MockApiClient.getMatchIncidentsFromFile();
		MatchEventStatistics matchStatisticsFromFile = MockApiClient.getMatchStatisticsFromFile();
		
		events.getData().forEach(e -> 
			{ 	
				FootballApiCache.INCIDENTS_PER_EVENT.putIfAbsent(e.getId(), matchIncidentsFromFile);
				FootballApiCache.STATS_PER_EVENT.putIfAbsent(e.getId(), matchStatisticsFromFile);
			}
		);
		
		
		return events;
	}
	
//	/**
//	 * Gets a list of the leagues for the countries we support.
//	 * 
//	 * @throws IOException
//	 * @throws ParseException 
//	 * @throws URISyntaxException 
//	 * @throws InterruptedException 
//	 */
//	public static Events getLiveEvents() throws IOException, ParseException, InterruptedException, URISyntaxException {
//		String url = SportScoreApiConstants.GET_LIVE_EVENTS_BY_SPORT_URL;
//		String content = new HttpHelper().fetchGetContentWithHeaders(url);
//		
//		Events events= new Gson().fromJson(content, new TypeToken<Events>() {}.getType());
//		events.getData().forEach(e->e.getStart_at());//TODO: not here
//		return events;
//	}
//	
	public static League getLeagueById(Integer leagueId) throws IOException {
		String url = SportScoreApiConstants.GET_LEAGUE_BY_ID_URL + leagueId;
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		
		League league= new Gson().fromJson(content, new TypeToken<League>() {}.getType());
		return league;
	}

	public static List<Section> getSections() throws IOException {
		List<Section> allSections = new ArrayList<>();
		
		String url = SportScoreApiConstants.GET_SECTIONS_BY_SPORT_URL;
		for (int page = 1; page <=3; page++) {
			String content = new HttpHelper().fetchGetContentWithHeaders(url+page);
			Sections pageSections = new Gson().fromJson(content, new TypeToken<Sections>() {}.getType());
			allSections.addAll(pageSections.getData());
		}

		return allSections;
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
	public static MatchEventIncidents getIncidents(int eventId) throws IOException, ParseException, InterruptedException, URISyntaxException {
		String url = SportScoreApiConstants.GET_EVENT_INCIDENTS_URL.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(eventId));
		
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		MatchEventIncidents incidents= new Gson().fromJson(content, new TypeToken<MatchEventIncidents>() {}.getType());
		return incidents;
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
	public static MatchEventStatistics getStatistics(int eventId) throws IOException, ParseException, InterruptedException, URISyntaxException {
		String url = SportScoreApiConstants.GET_EVENT_STATISTICS_URL.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(eventId));
		
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		MatchEventStatistics stats= new Gson().fromJson(content, new TypeToken<MatchEventStatistics>() {}.getType());
		return stats;
	}

	public static Season getCurrentSeason(Integer leagueId) {//31497
		String url = SportScoreApiConstants.GET_SEASONS_BY_LEAGUE_URL.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(leagueId));
		
		String content = null;
		try {
			content = new HttpHelper().fetchPostContentWithHeaders(url);
		} catch (IOException e) {
		
			System.out.println("GET SEASONS ERROR: " + e.getStackTrace());
		}
		Seasons seasons = new Gson().fromJson(content, new TypeToken<Seasons>() {}.getType());
		if (seasons == null || seasons.getData() == null || seasons.getData().isEmpty()) {
			return new Season();
		}
		
		Season currentSeason = seasons.getData().stream().max(Comparator.comparing(s -> s.getYear_end())).get();
		return currentSeason;		
	}
	
	public static StandingTable getSeasonStandings(Integer seasonId) {
		String url = SportScoreApiConstants.GET_SEASON_TABLE_STANDINGS.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(seasonId));
		
		String content = null;
		try {
			content = new HttpHelper().fetchGetContentWithHeaders(url);
		} catch (IOException e) {
		
			System.out.println("GET TABLES ERROR: " + e.getStackTrace());
		}
		StandingTables standingTables = new Gson().fromJson(content, new TypeToken<StandingTables>() {}.getType());
		if (standingTables == null || standingTables.getData() == null || standingTables.getData().isEmpty()) {
			return new StandingTable();
		}
		
		StandingTable currentTable = standingTables.getData().get(0);



		return currentTable;
		
	}
	
	public static Players getPlayersByTeamId(Integer teamId) {
		String url = SportScoreApiConstants.GET_PLAYERS_BY_TEAM_ID.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(teamId));
		
		String content = null;
		try {
			content = new HttpHelper().fetchGetContentWithHeaders(url);
		} catch (IOException e) {
			System.out.println("GET PLAYERS ERROR: " + e.getStackTrace());
			return new Players();
		}
		Players players = new Gson().fromJson(content, new TypeToken<Players>() {}.getType());
		if (players == null || players.getData() == null || players.getData().isEmpty()) {
			return new Players();
		}

		return players;
	}
	
	public static PlayerStatistics getPlayerStatistics(Integer playerId) {
		String url = SportScoreApiConstants.GET_STATISTICS_BY_PLAYER_ID.replace(SportScoreApiConstants.REPLACEMENT, String.valueOf(playerId));
		
		String content = null;
		try {
			content = new HttpHelper().fetchGetContentWithHeaders(url);
		} catch (IOException e) {
			System.out.println("GET PLAYER STATS ERROR: " + e.getStackTrace());
			return new PlayerStatistics();
		}
		//com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Expected BEGIN_ARRAY but was BEGIN_OBJECT at line 1 column 91 path $.data[0].details[0].statistics_items[0]
		PlayerStatistics playerStats = new Gson().fromJson(content, new TypeToken<PlayerStatistics>() {}.getType());
		if (playerStats == null || playerStats.getData() == null || playerStats.getData().isEmpty()) {
			return new PlayerStatistics();
		}

		return playerStats;
	}
	
}
