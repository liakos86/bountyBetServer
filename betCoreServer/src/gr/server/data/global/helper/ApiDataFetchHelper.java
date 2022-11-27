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
	public static void fetchEventsIntoLeagues(boolean live) {
		List<MatchEvent> events = new ArrayList<>();
		try {
			events = SportScoreClient.getEvents(live).getData();
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("LIVE EVENTS ERROR " + live);
			e.printStackTrace();
			return;
		}
		
		if (live) {
			
			/**
			 * TODO: fixme
			 */
			
			MatchEventIncidents matchEventIncidents = MockApiClient.getMatchIncidentsFromFile();
			events.forEach(e-> e.setIncidents(matchEventIncidents));
			
			System.out.println("LIVE ARE " + events.size());
			events.forEach(e->calculateLiveMinute(e));
			splitEventsIntoLiveLeagues(events);
		}else {
			splitEventsIntoLeagues(events);
		}

	}

	private static void calculateLiveMinute(MatchEvent event) {
		
		if (!"inprogress".equals(event.getStatus())) {
			return;
		}
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		long millisNow = cal.getTimeInMillis();
		
		//millisNow = Instant.now().getEpochSecond();
		
		String start_at = event.getStart_at();
		Date matchStart = null;
		try {
			matchStart = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT).parse(start_at);
		} catch (ParseException e) {
			return;
		}
		
		long millisMatchStart = matchStart.getTime();
		
		
		
		long millisLive = millisNow - millisMatchStart;
		if (event.getId().equals(1240160)) {
			System.out.println("match start is " + matchStart);
			System.out.println("now is " + cal.getTime());
		System.out.println("millis start " + millisMatchStart + " millis now " + millisNow);
		
		}
		int minuteLive = (int) (millisLive / 60000);
		
		event.setStatus_for_client(minuteLive + "'");
		
	}

	private static void splitEventsIntoLiveLeagues(List<MatchEvent> events) {
		for (MatchEvent matchEvent : events) {
			fixMatchMinute(matchEvent);
			
			League matchLeague = matchEvent.getLeague();
			Map<Integer, MatchEvent> leagueEvents = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague);
			if (leagueEvents == null) {
				RestApplication.LIVE_EVENTS_PER_LEAGUE.put(matchLeague, new HashMap<>());
			}

			RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague).put(matchEvent.getId(), matchEvent);
		}
	}

	private static void fixMatchMinute(MatchEvent matchEvent) {
		if (! "inprogress".equals(matchEvent.getStatus())) {
			return;
		}
		
	}

	private static void splitEventsIntoLeagues(List<MatchEvent> events) {
		Map<League, Map<Integer, MatchEvent>> leaguesWithEvents = new HashMap<>();
		for (MatchEvent event : events) {
			League league = RestApplication.LEAGUES.get(event.getLeague_id());
			if (league == null) {
				league = event.getLeague();
			}
			
			if (!leaguesWithEvents.containsKey(league)) {
				leaguesWithEvents.put(league, new HashMap<>());
			}
			
			leaguesWithEvents.get(league).put(event.getId(), event);
		}
		
		RestApplication.EVENTS_PER_DAY_PER_LEAGUE.put(DateUtils.todayStr(), leaguesWithEvents);
		
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
