package gr.server.data.global.helper.mock;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Sections;
import gr.server.impl.client.MockApiClient;


public class MockApiDataFetchHelper {

	public static void fetchLeagues() {
		Leagues leaguesFromFile = MockApiClient.getLeaguesFromFile();
		for (League league : leaguesFromFile.getData()) {
			if (FootballApiCache.PRIORITIES_OVERRIDDE.containsKey(league.getId())) {
				league.setPriority(FootballApiCache.PRIORITIES_OVERRIDDE.get(league.getId()));
			}
			FootballApiCache.ALL_LEAGUES.put(league.getId(), league);		
		}

		League zeroLeague = new League();
		zeroLeague.setName("Other Matches");
		FootballApiCache.ALL_LEAGUES.put(0, zeroLeague);		
	}

	public static void fetchSections() {
		Sections sectionsFromFile = MockApiClient.getSectionsFromFile();
		sectionsFromFile.getData().forEach(l -> FootballApiCache.ALL_SECTIONS.put(l.getId(), l));
	}
	
	public static Events fetchEvents(String filename) {
		Events eventsFromFile = MockApiClient.getEventsFromFile(filename);
		return eventsFromFile;
	}

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

}
