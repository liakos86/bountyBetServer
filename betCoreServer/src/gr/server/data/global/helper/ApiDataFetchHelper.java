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
import java.util.stream.Collectors;

import javax.jms.JMSException;

import gr.server.application.BetServerContextListener;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.events.PlayerStatistics;
import gr.server.data.api.model.events.Players;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.StandingRow;
import gr.server.data.api.model.league.StandingTable;
import gr.server.data.api.model.league.Team;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.live.helper.LiveUpdatesHelper;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.SportScoreClient;

public class ApiDataFetchHelper {

	public static void fetchLeagueStandings() {
		for (Entry<Integer, League> leagueEntry : FootballApiCache.ALL_LEAGUES.entrySet()) {
			
			
			//TODO reducing calls for testing
			if (FootballApiCache.SPAIN_LA_LIGA == leagueEntry.getKey().intValue()) {
				Season currentSeason = null;
				
				if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					currentSeason = MockApiClient.getSeasonsFromFile().getData().get(0);
				}else {
					currentSeason = SportScoreClient.getCurrentSeason(leagueEntry.getKey());
				}
				
				if (currentSeason == null) {
					continue;
				}
							
				StandingTable standingTable = null;
				if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					standingTable = MockApiClient.getStandingTableFromFile();
				}else {
					standingTable = SportScoreClient.getSeasonStandings(currentSeason.getId());					
				}
				
				currentSeason.setStandingTable(standingTable);
				
//				LeagueWithData leagueWithData = leagueEntry.getValue();
				
				currentSeason.setLeague(leagueEntry.getValue());
				List<Season> leagueSeasons = new ArrayList<>();
				leagueSeasons.add(currentSeason);
				
				leagueEntry.getValue().getSeasonIds().add(currentSeason.getId());
				
				FootballApiCache.SEASONS_PER_LEAGUE.put(leagueEntry.getKey(), leagueSeasons);
			}
		}
		
		//TODO: all leagues have the same standings for testing purposes
		List<Season> seasons = FootballApiCache.SEASONS_PER_LEAGUE.get(FootballApiCache.SPAIN_LA_LIGA);
		for (Entry<Integer, League> leagueEntry : FootballApiCache.ALL_LEAGUES.entrySet()) {
			FootballApiCache.SEASONS_PER_LEAGUE.put(leagueEntry.getKey(), seasons);		
			leagueEntry.getValue().getSeasonIds().add(seasons.get(0).getId());
		}
		
	}
	
	/**
	 * Gets the football events for multiple days.
	 * 
	 */
	public static void fetchEventsIntoLeaguesAndExtractMatchEvents() {
		Map<Integer, Date> datesToFetch = DateUtils.getDatesToFetch();
		
		for (Entry<Integer, Date> dateEntry : datesToFetch.entrySet()) {
			List<MatchEvent> events = eventsForDate(dateEntry.getValue());
			
			//TODO: We should get this info from websocket
//				try {
					System.out.println("GETTING EVENTS");
//					new LiveUpdatesHelper().updateEventsAndPublishFirebaseTopicMessages(events);
					splitEventsIntoLeaguesAndDays(dateEntry.getKey(), events);
//				} catch (JMSException e) {
//					System.out.println("JMS ERRORRRRRRRRR");
//					e.printStackTrace();
//				}; 
			};

	}
	
	public static List<MatchEvent> eventsForDate(Date date){
		List<MatchEvent> events = new ArrayList<>();
		
		try {
			if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
				throw new RuntimeException("MOCK ENABLED");
//				events = MockApiClient.getEventsFromFile("events1").getData();
			}else {
				//events = MockApiClient.getEventsFromFile("events1").getData();
				//if (false)
				events = SportScoreClient.getEvents(date).getData();
			}
			
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("EVENTS ERROR " + date);
			e.printStackTrace();
		}
		
		return events;
	}

	

	/**
	 * Gets the football events .
	 * If live events are requested {@link SportScoreApiConstants#GET_LIVE_EVENTS_BY_SPORT_URL}.
	 * If not, {@link SportScoreApiConstants#GET_EVENTS_BY_SPORT_DATE_URL} is used with 'today' as param.
	 * The latter will contain live events also.
	 * 
	 * First we obtain all the events for today with live=false.
	 * Then we obtain the live events.
	 * Every live event that was also retrieved in the first step will be replaced.
	 * 
	 * @param live
	 */
	public static void fetchLiveEventsIntoLeagues() {
		List<MatchEvent> events = new ArrayList<>();
		try {
			events = SportScoreClient.getLiveEvents().getData();
			new LiveUpdatesHelper().updateEventsAndPublishFirebaseTopicMessages(events);
			splitEventsIntoLiveLeagues(events);
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("LIVE EVENTS ERROR ");
			e.printStackTrace();
			return;
		} catch (JMSException e) {
			System.out.println("JMS ERROR ");
			e.printStackTrace();
		}

	}

	private static void splitEventsIntoLiveLeagues(List<MatchEvent> events) {
		for (MatchEvent matchEvent : events) {
			
			if (MatchEventStatus.FINISHED == MatchEventStatus.fromStatusText(matchEvent.getStatus())
					&& matchEvent.getWinner_code() != 0) {
				try {
					FootballApiCache.FINISHED_EVENTS.put(matchEvent);
					System.out.println("ADDING TO FINISHED " + matchEvent);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (!MatchEventStatus.INPROGRESS.equals(MatchEventStatus.fromStatusText(matchEvent.getStatus()))) {
				FootballApiCache.LIVE_EVENTS.remove(matchEvent.getId());
				continue;
			}
			
			League matchLeague = FootballApiCache.ALL_LEAGUES.get(matchEvent.getLeague_id());
			if (matchLeague == null) {
				continue;
			}
			
			LeagueWithData leagueWithData = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.get(0).get(matchLeague.getId());
			if (!leagueWithData.getMatchEvents().contains(matchEvent)) {
				leagueWithData.getMatchEvents().add(matchEvent);
			}
			
			MatchEvent liveEventFromCache = FootballApiCache.LIVE_EVENTS.get(matchEvent.getId());
			if (liveEventFromCache == null) {
				FootballApiCache.LIVE_EVENTS.put(matchEvent.getId(), matchEvent);
				continue;
			}
		
			liveEventFromCache.deepCopy(matchEvent);
		}		
	}

	
	/**
	 * Input is a date with all its games.
	 * We find the league that every game belongs to and add the game to that league's games.
	 * 
	 * @param date
	 * @param incomingEvents
	 */
	private static void splitEventsIntoLeaguesAndDays(Integer position, List<MatchEvent> incomingEvents) {
		
		//1. split the list of incoming events into a map of events per league
		Map<Integer, LeagueWithData> incomingLeagueData = new HashMap<>();

		for (MatchEvent incomingEvent : incomingEvents) {
			
			if (MatchEventStatus.FINISHED == MatchEventStatus.fromStatusText(incomingEvent.getStatus())
					&& incomingEvent.getWinner_code() != 0) {
				try {
					System.out.println("ADDING TO FINISHED " + incomingEvent);
					FootballApiCache.FINISHED_EVENTS.put(incomingEvent);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			League cachedLeague = FootballApiCache.ALL_LEAGUES.get(incomingEvent.getLeague_id());
			//TODO: can be null because we fetch mock leagues to reduce calls, but we fetch real events
			if (cachedLeague == null) {
				continue;
			}
			
			saveOrUpdateMatchInJvmMemory(incomingEvent);
			
			LeagueWithData incomingLeagueEvents = incomingLeagueData.get(incomingEvent.getLeague_id());
			
			if (incomingLeagueEvents == null) {
				incomingLeagueEvents = new LeagueWithData();
				incomingLeagueEvents.setLeagueId(cachedLeague.getId());
				incomingLeagueData.put(cachedLeague.getId(), incomingLeagueEvents);
			}
			
			incomingLeagueEvents.getMatchEvents().add(incomingEvent);
		}
				
		//2. replace the position's leagues with the new ones.
		List<LeagueWithData> existingLeaguesWithEvents = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.get(position);
		List<LeagueWithData> replacementLeaguesWithData = incomingLeagueData.values().stream().collect(Collectors.toList());
		if (existingLeaguesWithEvents == null || existingLeaguesWithEvents.isEmpty()) {			
			FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(position, replacementLeaguesWithData);
		}
		
		//TODO: filter differences between old and new lists of events.
	}

	private static void saveOrUpdateMatchInJvmMemory(MatchEvent incomingEvent) {
		if (!FootballApiCache.ALL_EVENTS.containsKey(incomingEvent.getId())) {
			FootballApiCache.ALL_EVENTS.put(incomingEvent.getId(), incomingEvent);
			return;
		}
		
		FootballApiCache.ALL_EVENTS.get(incomingEvent.getId()).deepCopy(incomingEvent);
		
	}

	public static void fetchSections() {
		try {
			SportScoreClient.getSections().forEach(s -> FootballApiCache.ALL_SECTIONS.put(s.getId(), s));
		} catch (IOException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

	public static void fetchPlayerStatistics() {//31730 laliga2024 31497
		
		Season season = FootballApiCache.SEASONS_PER_LEAGUE.get(FootballApiCache.SPAIN_LA_LIGA).get(0);
		
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
