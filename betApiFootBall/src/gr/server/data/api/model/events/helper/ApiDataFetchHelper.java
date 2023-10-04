package gr.server.data.api.model.events.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.impl.client.SportScoreClient;

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
				league = FootballApiCache.LEAGUES.get(event.getLeague_id());
				if (league == null) {//can be null because we fetch mock leagues to reduce calls
					System.out.println("CREATING DUMMY LEAGUE FOR " + event.getLeague_id());
					league = createDummyLeague();
					FootballApiCache.LEAGUES.put(event.getLeague_id(), league);
				}
				event.setLeague(league);
			}
			
			Map<Integer, MatchEvent> leagueEvents = leaguesWithEvents.get(league);
			if (leagueEvents == null) {
				leagueEvents = new HashMap<>();
				leaguesWithEvents.put(league, leagueEvents);
			}
			
			if (FootballApiCache.ALL_EVENTS.containsKey(event.getId())){
				leagueEvents.put(event.getId(), FootballApiCache.ALL_EVENTS.get(event.getId()));
			}else {
				leagueEvents.put(event.getId(), event);
			}
		}
		
		FootballApiCache.EVENTS_PER_DAY_PER_LEAGUE.put(position, leaguesWithEvents);
		
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
			SportScoreClient.getSections().forEach(s -> FootballApiCache.SECTIONS.put(s.getId(), s));
		} catch (IOException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

}
