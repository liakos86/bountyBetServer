package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Seasons;
import gr.server.data.api.model.league.Sections;
import gr.server.data.api.model.league.Standings;
import gr.server.data.api.model.league.Teams;
import gr.server.util.MockHttpHelper;

public class MockApiClient {

	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public static Map<String, Map<League, Map<Integer, MatchEvent>>> getEventsFromFile(String fileName)
			throws IOException, ParseException, InterruptedException, URISyntaxException {
		System.out.println("GETTING MOCK EVENTS FROM FILE " + fileName);

		Events allEvents = new Events();

		for (int i = 1; i < 2; i++) {

			try {

				String content = new MockHttpHelper().mockGetContentWithHeaders(fileName + i + ".json");
				System.out.println("GOT CONTENT FOR " + fileName + i + ".json");
				Events events = new Gson().fromJson(content, new TypeToken<Events>() {
				}.getType());
				
				System.out.println("GOT GSON FOR " + fileName + i + ".json");
				allEvents.getData().addAll(events.getData());
			} catch (Exception e) {
				System.out.println("ERROR FOR " + fileName + i + ".json");
				System.out.println(e.getMessage());
				continue;
			}
		}

		Map<String, Map<League, Map<Integer, MatchEvent>>> map = new HashMap<>();
		League randomLeague = allEvents.getData().get(0).getLeague();
		
		Map<Integer, MatchEvent> eventsMap = new HashMap<>();
		allEvents.getData().forEach(e -> eventsMap.put(e.getId(), e));
		
		Map<League, Map<Integer, MatchEvent>> leaguesMap = new HashMap<>();
		leaguesMap.put(randomLeague, eventsMap);
		map.put(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), leaguesMap);
		
		return map;
	}

	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public static Leagues getLeaguesFromFile()
			 {
		System.out.println("GETTING MOCK API LEAGUES");

		Leagues leagues = new Leagues();

		for (int i = 1; i < 20; i++) {

			try {
				String content = new MockHttpHelper().mockGetContentWithHeaders("leagues" + i + ".json");
				Leagues leaguesFromUrl = new Leagues();
				leaguesFromUrl = new Gson().fromJson(content, new TypeToken<Leagues>() {
				}.getType());
				leagues.getData().addAll(leaguesFromUrl.getData());
			} catch (Exception e) {
				continue;
			}
		}
		
		leagues.getData().forEach(l-> l.setName(l.getName_translations().get("en")));
		
		leagues.getData().forEach(l-> {if (l.getId() ==0) {System.out.println("League " + l.getName());}} );
		
		return leagues;

	}

	public static Teams getTeamsFromFile() {
		System.out.println("GETTING MOCK API TEAMS");

		Teams teams = new Teams();

		for (int i = 1; i < 10; i++) {

			try {
				String content = new MockHttpHelper().mockGetContentWithHeaders("teams" + i + ".json");
				Teams teamsFromUrl = new Teams();
				teamsFromUrl = new Gson().fromJson(content, new TypeToken<Teams>() {
				}.getType());
				teams.getData().addAll(teamsFromUrl.getData());
			} catch (Exception e) {
				continue;
			}
		}
		return teams;

	}

	public static Sections getSectionsFromFile() {
		System.out.println("GETTING MOCK API SECTIONS");

		Sections sections = new Sections();

		for (int i = 1; i < 5; i++) {

			try {
				String content = new MockHttpHelper().mockGetContentWithHeaders("sections" + i + ".json");
				Sections sectionsInner = new Sections();
				sectionsInner = new Gson().fromJson(content, new TypeToken<Sections>() {}.getType());
				sections.getData().addAll(sectionsInner.getData());
			} catch (Exception e) {
				continue;
			}
		}
		return sections;
	}
	
	public static Standings getStandingsFromFile() {
		Standings standings = new Standings();
		for (int i = 1; i < 2; i++) {
			try {
				String content = new MockHttpHelper().mockGetContentWithHeaders("standings" + i + ".json");
				Standings stds = new Standings();
				stds = new Gson().fromJson(content, new TypeToken<Standings>() {}.getType());
				standings.getData().addAll(stds.getData());
			} catch (Exception e) {
				continue;
			}
		}
		return standings;
	}

	public static Seasons getSeasonsFromFile() {
		Seasons seasons = new Seasons();
		for (int i = 1; i < 2; i++) {
			try {
				String content = new MockHttpHelper().mockGetContentWithHeaders("seasons" + i + ".json");
				Seasons stds = new Seasons();
				stds = new Gson().fromJson(content, new TypeToken<Seasons>() {}.getType());
				seasons.getData().addAll(stds.getData());
			} catch (Exception e) {
				continue;
			}
		}
		return seasons;
	}
	
	public static MatchEventIncidents getMatchIncidentsFromFile() {
		try {
			String content = new MockHttpHelper().mockGetContentWithHeaders("eventIncidents.json");
			System.out.println(content);
			MatchEventIncidents incidents = new Gson().fromJson(content, new TypeToken<MatchEventIncidents>() {}.getType());
			return incidents;
		} catch (Exception e) {
			return null;
		}
	}

}
