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

import gr.server.application.RestApplication;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.impl.client.SportScoreClient;
import gr.server.util.DateUtils;

public class ApiDataFetchHelper {

	public static void fetchLeagueStandings() {
		// TODO Auto-generated method stub
		
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
			events.forEach(e -> { if (!RestApplication.ALL_EVENTS.containsKey(e.getId())) {
											RestApplication.ALL_EVENTS.put(e.getId(), e);
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
				league = RestApplication.LEAGUES.get(event.getLeague_id());
				if (league == null) {//can be null because we fetch mock leagues to reduce calls
					System.out.println("CREATING DUMMY LEAGUE FOR " + event.getLeague_id());
					league = createDummyLeague();
					RestApplication.LEAGUES.put(event.getLeague_id(), league);
				}
				event.setLeague(league);
			}
			
			Map<Integer, MatchEvent> leagueEvents = leaguesWithEvents.get(league);
			if (leagueEvents == null) {
				leagueEvents = new HashMap<>();
				leaguesWithEvents.put(league, leagueEvents);
			}
			
			if (RestApplication.ALL_EVENTS.containsKey(event.getId())){
				leagueEvents.put(event.getId(), RestApplication.ALL_EVENTS.get(event.getId()));
			}else {
				leagueEvents.put(event.getId(), event);
			}
		}
		
		RestApplication.EVENTS_PER_DAY_PER_LEAGUE.put(position, leaguesWithEvents);
		
	}

	private static League createDummyLeague() {
		League league = new League(Integer.MIN_VALUE);
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
			SportScoreClient.getSections().forEach(s -> RestApplication.SECTIONS.put(s.getId(), s));
		} catch (IOException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

}
