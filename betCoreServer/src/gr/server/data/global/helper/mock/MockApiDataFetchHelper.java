package gr.server.data.global.helper.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

import gr.server.application.RestApplication;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Seasons;
import gr.server.data.api.model.league.Sections;
//import gr.server.data.api.model.league.StandingTable;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.Events;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.SportScoreClient;


public class MockApiDataFetchHelper {

	public static void fetchLeagues() {
		Leagues leaguesFromFile = MockApiClient.getLeaguesFromFile();
//		leaguesFromFile.getData().forEach(l -> RestApplication.LEAGUES.put(l.getId(), l));
		leaguesFromFile.getData().forEach(l -> FootballApiCache.LEAGUES.put(l.getId(), l));
	}

	public static void fetchSections() {
		Sections sectionsFromFile = MockApiClient.getSectionsFromFile();
		sectionsFromFile.getData().forEach(l -> FootballApiCache.SECTIONS.put(l.getId(), l));
//		sectionsFromFile.getData().forEach(l -> RestApplication.SECTIONS.put(l.getId(), l));
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

}
