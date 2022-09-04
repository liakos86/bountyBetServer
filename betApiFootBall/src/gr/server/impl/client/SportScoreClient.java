package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.league.Team;
import gr.server.data.api.model.league.Teams;
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
	public static Teams getTeams() throws IOException, ParseException, InterruptedException, URISyntaxException {
		System.out.println("GETTING API FOOTBALL TEAMS");
		String url = SportScoreApiConstants.GET_TEAMS_BY_SPORT_URL;

		/**
		 * TODO pass the url
		 */
		String content = new HttpHelper().fetchGetContentWithHeaders("teams.json");
		System.out.println(content);
		
		Teams teams= new Gson().fromJson(content, new TypeToken<Teams>() {}.getType());
		System.out.println(teams.getData().get(0).getName());
		return teams;

	}
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws InterruptedException 
	 */
	public static Events getEvents() throws IOException, ParseException, InterruptedException, URISyntaxException {
		System.out.println("GETTING API EVENTS");
		String url = SportScoreApiConstants.GET_EVENTS_BY_SPORT_DATE_URL;

		/**
		 * TODO pass the url
		 */
		String content = new HttpHelper().fetchGetContentWithHeaders("events.json");
		System.out.println(content);
		
		Events events= new Gson().fromJson(content, new TypeToken<Events>() {}.getType());
		System.out.println(events.getData().get(0).getHome_team().getName());
		return events;

	}

}
