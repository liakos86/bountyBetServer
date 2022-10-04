package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.server.application.RestApplication;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.impl.client.SportScoreClient;
import gr.server.util.DateUtils;

public class ApiDataFetchHelper {

	public static void fetchEvents(boolean live) {
	//	System.out.println("REFRESHING LIVE EVENTS " + live + " ON: " + new Date() + "n" + "Thread's name: " + Thread.currentThread().getName());

		List<MatchEvent> events = new ArrayList<>();
		try {
			events = SportScoreClient.getEvents(live).getData();
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("LIVE EVENTS ERROR " + live);
			e.printStackTrace();
			return;
		}
		
		if (!live) {
			splitEventsIntoLeagues(events);
			return;
		}
		
		for (MatchEvent matchEvent : events) {
			League matchLeague = matchEvent.getLeague();
			Map<Integer, MatchEvent> leagueEvents = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague);
			if (leagueEvents == null) {
				RestApplication.LIVE_EVENTS_PER_LEAGUE.put(matchLeague, new HashMap<>());
			}

			RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague).put(matchEvent.getId(), matchEvent);
			RestApplication.TODAY_EVENTS_PER_ID.put(matchEvent.getId(), matchEvent);
			System.out.println("ADDED EVENT TO " + matchLeague);
		}

	}

	private static void splitEventsIntoLeagues(List<MatchEvent> events) {
		Map<League, Map<Integer, MatchEvent>> leaguesWithEvents = new HashMap<>();
		for (MatchEvent event : events) {
			League league = event.getLeague();
			if (!leaguesWithEvents.containsKey(league)) {
				leaguesWithEvents.put(league, new HashMap<>());
			}
			
			leaguesWithEvents.get(league).put(event.getId(), event);
		}
		
		RestApplication.EVENTS_PER_DAY_PER_LEAGUE.put(DateUtils.todayStr(), leaguesWithEvents);
		
	}

}
