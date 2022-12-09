package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import gr.server.application.RestApplication;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.league.League;
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
				events.forEach(e -> RestApplication.ALL_EVENTS.put(e.getId(), e));
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
		
		System.out.println("LIVE ARE " + events.size());
		events.forEach(e->calculateLiveMinute(e));
		splitEventsIntoLiveLeagues(events);

	}


	private static void calculateLiveMinute(MatchEvent matchEvent) {
		if (! "inprogress".equals(matchEvent.getStatus())) {
			return;
		}
		
		SimpleDateFormat matchTimeFormat = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT);
		matchTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		try {
			Date matchTime = matchTimeFormat.parse(matchEvent.getStart_at());
			long x = new Date().getTime() - matchTime.getTime();
			matchEvent.setTime_live(x/60000);
			matchEvent.setStatus_for_client(x/60000 + "'");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
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

	private static void splitEventsIntoLeaguesAndDays(Date date, List<MatchEvent> events) {
		Map<League, Map<Integer, MatchEvent>> leaguesWithEvents = new HashMap<>();
		for (MatchEvent event : events) {
			League league = event.getLeague();// RestApplication.LEAGUES.get(event.getLeague_id());
			if (league == null) {
				league = event.getLeague();
			}
			
			if (!leaguesWithEvents.containsKey(league)) {
				leaguesWithEvents.put(league, new HashMap<>());
			}
			
			leaguesWithEvents.get(league).put(event.getId(), event);
		}
		
		RestApplication.EVENTS_PER_DAY_PER_LEAGUE.put(DateUtils.dateStr(date), leaguesWithEvents);
		
	}

	public static void fetchSpecificLeague(Integer league_id) {
		
		Map<Integer, MatchEvent> leagueMap = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(new League(league_id));
		if (leagueMap != null) {
			System.out.println("LEAGUE EXISTS " + league_id);
			return;
		}

		try {
			League newLeague = SportScoreClient.getLeagueById(league_id);
			RestApplication.LIVE_EVENTS_PER_LEAGUE.put(newLeague, new HashMap<>());
		} catch (IOException e) {
			System.out.println("FETCH LEAGUE ERROR " + league_id);
			e.printStackTrace();
		}
		
	}

}
