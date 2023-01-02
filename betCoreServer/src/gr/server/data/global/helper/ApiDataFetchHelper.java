package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.server.application.RestApplication;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.impl.client.MockApiClient;
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
		List<Date> datesToFetch = getDatesToFetchList();
		
		for (Date date : datesToFetch) {
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
				return;
			}
			
			splitEventsIntoLeaguesAndDays(date, events);
		}

	}

	private static List<Date> getDatesToFetchList() {
		List<Date> datesToFetch = new ArrayList<>();
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.DATE, -1);
		Date yesterday = instance.getTime();
		datesToFetch.add(yesterday);
		datesToFetch.add(new Date());
		instance.add(Calendar.DATE, 2);
		Date tomorrow = instance.getTime();
		datesToFetch.add(tomorrow);
		return datesToFetch;
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
			events.forEach(e -> RestApplication.ALL_EVENTS.put(e.getId(), e));
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("LIVE EVENTS ERROR ");
			e.printStackTrace();
			return;
		}
		
		/**
		 * TODO: fixme
		 */
		
		MatchEventIncidents matchEventIncidents = MockApiClient.getMatchIncidentsFromFile();
		events.forEach(e-> e.setIncidents(matchEventIncidents));
		
		events.forEach(e-> RestApplication.MINUTE_TRACKER.track(e));
		
		splitEventsIntoLiveLeagues(events);

	}

	private static void splitEventsIntoLiveLeagues(List<MatchEvent> events) {
		for (MatchEvent matchEvent : events) {
			League matchLeague = matchEvent.getLeague();
			Map<Integer, MatchEvent> leagueEvents = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague);
			if (leagueEvents == null) {
				RestApplication.LIVE_EVENTS_PER_LEAGUE.put(matchLeague, new HashMap<>());
			}

			RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague).put(matchEvent.getId(), matchEvent);
		}
	}

	/**
	 * Input is a date with all its games.
	 * We find the league that every game belongs to and add the game to that league's games.
	 * TODO: a null league appears in some games. we create a dummy league and assign all those games.
	 * 
	 * @param date
	 * @param events
	 */
	private static void splitEventsIntoLeaguesAndDays(Date date, List<MatchEvent> events) {
		Map<League, Map<Integer, MatchEvent>> leaguesWithEvents = new HashMap<>();
		for (MatchEvent event : events) {
			League league = event.getLeague();
			if (league == null) {
				league = RestApplication.LEAGUES.get(event.getLeague_id());
				if (league == null) {
					league = createDummyLeague();
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
		
		RestApplication.EVENTS_PER_DAY_PER_LEAGUE.put(DateUtils.dateStr(date), leaguesWithEvents);
		
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
