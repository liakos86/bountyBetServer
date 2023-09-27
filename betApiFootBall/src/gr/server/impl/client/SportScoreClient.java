package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Sections;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.util.HttpHelper;

public class SportScoreClient {
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
//	public static Teams getTeams() throws IOException, ParseException, InterruptedException, URISyntaxException {
//		System.out.println("GETTING API FOOTBALL TEAMS");
//		String url = SportScoreApiConstants.GET_TEAMS_BY_SPORT_URL;
//
//		/**
//		 * TODO pass the url
//		 */
//		String content = new HttpHelper().fetchGetContentWithHeaders("teams1.json");
//		System.out.println(content);
//		
//		Teams teams= new Gson().fromJson(content, new TypeToken<Teams>() {}.getType());
//		System.out.println(teams.getData().get(0).getName());
//		return teams;
//
//	}
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
	public static Events getEvents(Date date) throws IOException, ParseException, InterruptedException, URISyntaxException {
		String url = SportScoreApiConstants.GET_EVENTS_BY_SPORT_DATE_URL;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.GET_EVENTS_DATE_FORMAT);
		String today = simpleDateFormat.format(date);
		url += today;
		
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		Events events= new Gson().fromJson(content, new TypeToken<Events>() {}.getType());
		return events;
	}
	
//	/**
//	 * Gets a list of the leagues for the countries we support.
//	 * 
//	 * @throws IOException
//	 * @throws ParseException 
//	 * @throws URISyntaxException 
//	 * @throws InterruptedException 
//	 */
//	public static Events getLiveEvents() throws IOException, ParseException, InterruptedException, URISyntaxException {
//		String url = SportScoreApiConstants.GET_LIVE_EVENTS_BY_SPORT_URL;
//		String content = new HttpHelper().fetchGetContentWithHeaders(url);
//		
//		Events events= new Gson().fromJson(content, new TypeToken<Events>() {}.getType());
//		events.getData().forEach(e->e.getStart_at());//TODO: not here
//		return events;
//	}
//	
	public static League getLeagueById(Integer leagueId) throws IOException {
		String url = SportScoreApiConstants.GET_LEAGUE_BY_ID_URL + leagueId;
		String content = new HttpHelper().fetchGetContentWithHeaders(url);
		
		League league= new Gson().fromJson(content, new TypeToken<League>() {}.getType());
		return league;
	}

	public static List<Section> getSections() throws IOException {
		List<Section> allSections = new ArrayList<>();
		
		String url = SportScoreApiConstants.GET_SECTIONS_BY_SPORT_URL;
		for (int page = 1; page <=3; page++) {
			String content = new HttpHelper().fetchGetContentWithHeaders(url+page);
			Sections pageSections = new Gson().fromJson(content, new TypeToken<Sections>() {}.getType());
			allSections.addAll(pageSections.getData());
		}

		return allSections;
	}
}
