package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.events.PlayerStatistics;
import gr.server.data.api.model.events.Players;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueInfo;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.StandingRow;
import gr.server.data.api.model.league.StandingTable;
import gr.server.data.api.model.league.Team;
import gr.server.impl.client.SportScoreClient;

public class ApiDataFetchHelper {

	public static void fetchLeagueStandings() {
		for (Entry<Integer, League> leagueEntry : FootballApiCache.LEAGUES.entrySet()) {
			if (FootballApiCache.SPAIN_LA_LIGA == leagueEntry.getKey()) {
				//TODO this is the premier league. testing purposes
				Season currentSeason = SportScoreClient.getCurrentSeason(leagueEntry.getKey());
				if (currentSeason == null) {
					continue;
				}
							
				StandingTable standingTable = SportScoreClient.getSeasonStandings(currentSeason.getId());
				currentSeason.setStandingTable(standingTable);
				
				
				League league = leagueEntry.getValue();
//				LeagueInfo leagueInfo = new LeagueInfo();
//				leagueInfo.setHas_logo(league.isHas_logo());
//				leagueInfo.setId(league.getId());
//				leagueInfo.setLogo(league.getLogo());
//				leagueInfo.setName(league.getName());
//				leagueInfo.setName_translations(league.getName_translations());
//				leagueInfo.setPriority(league.getPriority());
//				leagueInfo.setSection_id(league.getSection_id());
//				leagueInfo.setSection(league.getSection());
				currentSeason.setLeagueInfo((LeagueInfo)league);
				
				FootballApiCache.STANDINGS.put(leagueEntry.getKey(), currentSeason);
			}
		}
	}
	
	/**
	 * Gets the football events for multiple days.
	 * 
	 */
	public static void fetchEventsIntoLeagues() {
		Map<Integer, Date> datesToFetch = DateUtils.getDatesToFetch();
		
		for (Entry<Integer, Date> dateEntry : datesToFetch.entrySet()) {
			List<MatchEvent> events = eventsForDate(dateEntry.getValue());
			splitEventsIntoLeaguesAndDays(dateEntry.getKey(), events);
		}

	}
	
	public static List<MatchEvent> eventsForDate(Date date){
		List<MatchEvent> events = new ArrayList<>();
		
		
		try {
			
			events = SportScoreClient.getEvents(date).getData();
			events.forEach(e -> { if (!FootballApiCache.ALL_EVENTS.containsKey(e.getId())) {
										FootballApiCache.ALL_EVENTS.put(e.getId(), e);
									}
								}
			);
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("EVENTS ERROR " + date);
			e.printStackTrace();
		}
		
		return events;
	}

	

//	/**
//	 * Gets the football events .
//	 * If live events are requested {@link SportScoreApiConstants#GET_LIVE_EVENTS_BY_SPORT_URL}.
//	 * If not, {@link SportScoreApiConstants#GET_EVENTS_BY_SPORT_DATE_URL} is used with 'today' as param.
//	 * The latter will contain live events also.
//	 * 
//	 * First we obtain all the events for today with live=false.
//	 * Then we obtain the live events.
//	 * Every live event that was also retrieved in the first step will be replaced.
//	 * 
//	 * @param live
//	 */
//	public static void fetchLiveEventsIntoLeagues() {
//		List<MatchEvent> events = new ArrayList<>();
//		try {
//			events = SportScoreClient.getLiveEvents().getData();
//			events.forEach(e -> RestApplication.ALL_EVENTS.put(e.getId(), e));
//		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
//			System.out.println("LIVE EVENTS ERROR ");
//			e.printStackTrace();
//			return;
//		}
//		
//		/**
//		 * TODO: fixme
//		 */
//		
//		MatchEventIncidents matchEventIncidents = MockApiClient.getMatchIncidentsFromFile();
//		events.forEach(e-> e.setIncidents(matchEventIncidents));
//		
//		events.forEach(e-> RestApplication.MINUTE_TRACKER.track(e));
//		
//		splitEventsIntoLiveLeagues(events);
//
//	}

//	private static void splitEventsIntoLiveLeagues(List<MatchEvent> events) {
//		
//		
//		for (MatchEvent matchEvent : events) {
//			if (!MatchEventStatus.INPROGRESS.equals(MatchEventStatus.fromStatusText(matchEvent.getStatus()))) {
//				continue;
//			}
//			
//			System.out.println("********* LIVE MATCH FOUND");
			
//			League matchLeague = matchEvent.getLeague();
//			Map<Integer, MatchEvent> leagueEvents = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague);
//			if (leagueEvents == null) {
//				RestApplication.LIVE_EVENTS_PER_LEAGUE.put(matchLeague, new HashMap<>());
//			}

			//MatchEventIncidents matchEventIncidents = MockApiClient.getMatchIncidentsFromFile();
			//matchEvent.setIncidents(matchEventIncidents);
//			RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague).put(matchEvent.getId(), matchEvent);
			//RestApplication.MINUTE_TRACKER.track(matchEvent);
//		}
		
//		int liveLeagues = RestApplication.LIVE_EVENTS_PER_LEAGUE.size();
//		System.out.println("LIVE: " + liveLeagues);
		
//	}

	
	/**
	 * Input is a date with all its games.
	 * We find the league that every game belongs to and add the game to that league's games.
	 * 
	 * @param date
	 * @param events
	 */
	private static void splitEventsIntoLeaguesAndDays(Integer position, List<MatchEvent> events) {
		Map<League, Map<Integer, MatchEvent>> leaguesWithEvents = new HashMap<>();
		for (MatchEvent event : events) {
			League league = event.getLeague();
			if (league == null) {
//				league = RestApplication.LEAGUES.get(event.getLeague_id());
				league = FootballApiCache.LEAGUES.get(event.getLeague_id());
				if (league == null) {//can be null because we fetch mock leagues to reduce calls
					System.out.println("CREATING DUMMY LEAGUE FOR " + event.getLeague_id());
					league = createDummyLeague();
//					RestApplication.LEAGUES.put(event.getLeague_id(), league);
					FootballApiCache.LEAGUES.put(event.getLeague_id(), league);
				}
				event.setLeague(league);
			}
			
//			if (league.getName().contains("League")) {
//				continue;
//			}
			
			Map<Integer, MatchEvent> leagueEvents = leaguesWithEvents.get(league);
			if (leagueEvents == null) {
				leagueEvents = new HashMap<>();
				leaguesWithEvents.put(league, leagueEvents);
			}
			
			
			FootballApiCache.ALL_EVENTS.put(event.getId(), event);
//			if (FootballApiCache.ALL_EVENTS.containsKey(event.getId())){
//				leagueEvents.put(event.getId(), FootballApiCache.ALL_EVENTS.get(event.getId()));
//			}else {
				leagueEvents.put(event.getId(), event);
//			}
		}
		
		FootballApiCache.EVENTS_PER_DAY_PER_LEAGUE.put(position, leaguesWithEvents);
		
	}

	private static League createDummyLeague() {
		League league = new League();
		league.setId(Integer.MIN_VALUE);
		league.setName("Other");
		Section section = new Section();
		section.setId(Integer.MIN_VALUE);
		section.setName("Other Section");
		section.setPriority(0);
		section.setSport_id(1);
		league.setSection(section);
		
		league.setLogo("https://tipsscore.com/resb/no-league.png");
		league.setHas_logo(true);
		return league;
	}

	public static void fetchSections() {
		try {
			SportScoreClient.getSections().forEach(s -> FootballApiCache.SECTIONS.put(s.getId(), s));
		} catch (IOException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

	public static void fetchPlayerStatistics() {//31730 laliga2024 31497
		
		Season season = FootballApiCache.STANDINGS.get(251);
		
		//for (Entry<Integer, Season> seasonStandingEntry : FootballApiCache.STANDINGS.entrySet()) {
			//Season season = seasonStandingEntry.getValue();
			StandingTable standingTable = season.getStandingTable();
			Map<Player, PlayerStatistics> season_player_statistics = standingTable.getSeason_player_statistics();
			int rows = 0;
			for (StandingRow standingRow : standingTable.getStandings_rows()) {
				Team team = standingRow.getTeam();
				Players playersOfTeam = SportScoreClient.getPlayersByTeamId(team.getId());
				int i =0;
				for (Player player : playersOfTeam.getData()) {
					PlayerStatistics playerStatistics = SportScoreClient.getPlayerStatistics(player.getId());
					season_player_statistics.put(player, playerStatistics);
					
					if(i>1) {
						break;
					}
					++i;
					//TODO remove when ready
				}
				
				if (rows > 4) {
					break;
				}
			}
		//}	
		
	}

}
