package gr.server.data.global.helper.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.events.MatchEventStatistics;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Sections;
import gr.server.data.enums.MatchEventStatus;
import gr.server.impl.client.MockApiClient;


public class MockApiDataFetchHelper {

	public static void fetchLeagues() {
		Leagues leaguesFromFile = MockApiClient.getLeaguesFromFile();
		for (League league : leaguesFromFile.getData()) {
//			if (FootballApiCache.PRIORITIES_OVERRIDDE.containsKey(league.getId())) {
//				league.setPriority(FootballApiCache.PRIORITIES_OVERRIDDE.get(league.getId()));
//			}
			
			
			
			
			if (!FootballApiCache.SUPPORTED_SECTION_IDS.contains(league.getSection_id())) {
				continue;
			}
			
			FootballApiCache.ALL_LEAGUES.put(league.getId(), league);		
		}

//		League zeroLeague = new League();
//		zeroLeague.setName("Other Matches");
//		zeroLeague.setId(0);
//		FootballApiCache.ALL_LEAGUES.put(0, zeroLeague);		
	}

	public static void fetchSections() {
		Sections sectionsFromFile = MockApiClient.getSectionsFromFile();
		sectionsFromFile.getData().forEach(l -> {
		if(FootballApiCache.SUPPORTED_SECTION_IDS.contains(l.getId())) {
			FootballApiCache.ALL_SECTIONS.put(l.getId(), l);
		}
		});
	}
	
	public static Events fetchEvents(String filename) {
		Events eventsFromFile = MockApiClient.getEventsFromFile(filename);
		return eventsFromFile;
	}
	
//	boolean hasNoRed(int eventId) {
//		MatchEventIncidentsWithStatistics incidents = FootballApiCache.ALL_MATCH_STATS.get(eventId);
//		for (MatchEventIncident i : incidents.getMatchEventIncidents().getData()) {
//			if (i.getCard_type()!=null && i.getCard_type().equals("Red") && i.getPlayer_team()==1) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
	
//	public void mockRedCards() {
//		
//		try {
//			Set<MatchEvent> liveEvents = FootballApiCache.ALL_EVENTS.values().stream().filter(
//					m -> MatchEventStatus.INPROGRESS.getStatusStr().equals(m.getStatus()))
//					.collect(Collectors.toSet());
//			
//			
//			Player p = eventWithoutRed.getIncidents().getData().stream().filter(i ->
//			i.getPlayer() !=null).collect(Collectors.toList()).get(0).getPlayer();
//		
//			MatchEventIncident redCard = new MatchEventIncident();
//			redCard.setCard_type("Red");
//			redCard.setTime(5);
//			redCard.setOrder(1);
//			redCard.setEvent_id(eventWithoutRed.getId());
//			redCard.setIncident_type("card");
//			redCard.setPlayer_team(1);
//			redCard.setPlayer(p);
//			
//			eventWithoutRed.getIncidents().getData().add(redCard);
//			
//			System.out.println("RED ADDED TO " + eventWithoutRed.getHome_team().getName());
//			
//		
//		}catch(Exception e) {
//			
//		}
//		
//	}
	

//	static StandingTable fetchLeagueStandings() {
//		StandingTable standingsFromFile =  MockApiClient.getStandingsFromFile();
//		return standingsFromFile;
//	}

//	public static void fetchSeasonsStandingsIntoLeagues() {
////		Seasons seasonsFromFile = MockApiClient.getSeasonsFromFile();
////		for (Season s : seasonsFromFile.getData()) {
//		for (Entry<Integer, League> leagueEntry : FootballApiCache.LEAGUES.entrySet()) {
//			//League league = FootballApiCache.LEAGUES.get(s.getLeague_id());
//			if (FootballApiCache.ENGLAND_PREMIER_LEAGUE == leagueEntry.getKey()) {//TODO this is the premier league. testing purposes
//				StandingTable currentYearLeagueStandings = SportScoreClient.getCurrentSeasonStandings(leagueEntry.getKey());// fetchLeagueStandings();
////				Collections.sort(currentYearLeagueStandings.getData().get(0).getStandings_rows());
////				s.setStanding(currentYearLeagueStandings.getData().get(0));
////				if (league.getSeasons() == null) {
////					league.setSeasons(new ArrayList<>());
////				}
//				//league.getSeasons().add(s);
//			}
//		}
//	}
	
	private static League createDummyLeague() {
		League league = new League();
		league.setId(Integer.MIN_VALUE);
		league.setName("Other");
//		Section section = new Section();
//		section.setId(Integer.MIN_VALUE);
//		section.setName("Other Section");
//		section.setPriority(0);
//		section.setSport_id(1);
//		league.setSection(section);
		
		league.setLogo("https://tipsscore.com/resb/no-league.png");
		league.setHas_logo(true);
		return league;
	}

	public void updateLiveStats() {
		Set<MatchEvent> liveEvents = FootballApiCache.ALL_EVENTS.values().stream().filter(
				m -> MatchEventStatus.INPROGRESS.getStatusStr().equals(m.getStatus()))
		.collect(Collectors.toSet());
		
		try {
		
			//TODO: remove mock, do it for all live
			MatchEventIncidents incidents = MockApiClient.getMatchIncidentsFromFile();// getIncidents(2070730);
			MatchEventStatistics statistics = MockApiClient.getMatchStatisticsFromFile();// getStatistics(2070730);
			
			for (MatchEvent matchEvent : liveEvents) {
				MatchEventIncidentsWithStatistics matchEventIncidentsWithStatistics = new MatchEventIncidentsWithStatistics();
				matchEventIncidentsWithStatistics.setEventId(matchEvent.getId());
				matchEventIncidentsWithStatistics.setMatchEventIncidents(incidents);
				matchEventIncidentsWithStatistics.setMatchEventStatistics(statistics);
				
				FootballApiCache.ALL_MATCH_STATS.put(matchEvent.getId(), matchEventIncidentsWithStatistics);
			}
			
//			logger.log(Level.INFO, "LIVE INCIDENTS:" + incidents.getData().size());
//			logger.log(Level.INFO, "LIVE STATS:" + statistics.getData().size());
		
		}catch(Exception e) {
//			logger.log(Level.ERROR, "ERROR LIVE INCIDENTS:" + incidents.getData().size());
		}	
	}

	/**
	 * Gets the football events for multiple days.
	 * 
	 */
	public static void fetchEventsIntoLeagues() {
		Map<Integer, Date> datesToFetch = DateUtils.getDatesToFetch();
		
		for (Entry<Integer, Date> dateEntry : datesToFetch.entrySet()) {
			Events fetchEvents = fetchEvents("events1");
			splitEventsIntoLeaguesAndDays(dateEntry.getKey(), fetchEvents.getData());
		}

	}
	
	/**
	 * Input is a date with all its games.
	 * We find the league that every game belongs to and add the game to that league's games.
	 * 
	 * @param date
	 * @param events
	 */
	private static void splitEventsIntoLeaguesAndDays(Integer position, List<MatchEvent> events) {
		
				
		Map<Integer, LeagueWithData> incomingLeagueData = new HashMap<>();
		

		for (MatchEvent event : events) {
			
			FootballApiCache.ALL_EVENTS.put(event.getId(), event);
			
			League cachedLeague = FootballApiCache.ALL_LEAGUES.get(event.getLeague_id());
					
			//TODO: can be null because we fetch mock leagues to reduce calls, but we fetch real events
			if (cachedLeague == null) {
				continue;
			}
			
			LeagueWithData incomingLeagueEvents = incomingLeagueData.get(event.getLeague_id());
			
			if (incomingLeagueEvents == null) {
				incomingLeagueEvents = new LeagueWithData();
				incomingLeagueEvents.setLeagueId(cachedLeague.getId());
				incomingLeagueData.put(cachedLeague.getId(), incomingLeagueEvents);
			}
			
			incomingLeagueEvents.getMatchEvents().add(event);
		}
				

		List<LeagueWithData> replacementLeaguesWithData = incomingLeagueData.values().stream().collect(Collectors.toList());
		
		FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(position, replacementLeaguesWithData);
	}

}
