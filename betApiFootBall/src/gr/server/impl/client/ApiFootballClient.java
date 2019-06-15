package gr.server.impl.client;

import gr.server.data.api.model.events.Event;
import gr.server.data.api.model.league.League;
import gr.server.data.constants.ApiFootBallConstants;
import gr.server.data.enums.SupportedCountry;
import gr.server.util.HttpHelper;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ApiFootballClient {
	
	/**
	 * Gets a list of the leagues for the countries we support.
	 * 
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static List<League> getLeagues() throws IOException, ParseException {
		List<League> leagues = new ArrayList<League>();
		for (SupportedCountry country : SupportedCountry.values()) {
			String url = ApiFootBallConstants.GET_LEAGUES_FOR_COUNTRY_URL + country.getCountryId() + ApiFootBallConstants.API_FOOTBALL_KEY;
			String content = new HttpHelper().fetchGetContent(url);
			List<League> leaguesOfCountry = new Gson().fromJson(content,new TypeToken<List<League>>() {}.getType());
			List<League> leaguesWithEvents = getEventsFor(leaguesOfCountry);
			leagues.addAll(leaguesWithEvents);
		}
		return leagues;
		
	}

	private static List<League> getEventsFor(List<League> leagues) throws IOException, ParseException {
		
		List<League> leaguesWithEvents = new ArrayList<League>();
		for (League league : leagues) {
			String url = ApiFootBallConstants.GET_EVENTS_FOR_DATES + ApiFootBallConstants.LEAGUE_URL + league.getLeagueId() + ApiFootBallConstants.API_FOOTBALL_KEY;
			
			//Calendar calendar = Calendar.getInstance();
			//Date today = calendar.getTime();
			//DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
			String todayString = "2019-05-29";//dateFormat.format(today);
			
			//calendar.add(Calendar.DAY_OF_MONTH, 2);
			String endDayString = "2019-05-30";// dateFormat.format(calendar.getTime());
			
			url = url.replace(ApiFootBallConstants.REPLACE_DATE_FROM, todayString).replace(ApiFootBallConstants.REPLACE_DATE_TO, endDayString);
			
			String content = new HttpHelper().fetchGetContent(url);
			
			if (content.contains("error")){
				league.setEvents(new ArrayList<Event>());
				continue;
			}
			
			Gson gson = new Gson();
			List<Event> events = gson.fromJson(content,new TypeToken<List<Event>>() {}.getType());
			league.setEvents(events);
			
			if (!events.isEmpty()){
				leaguesWithEvents.add(league);
			}
		}
		//getOddsFor(competitions);
		return leaguesWithEvents;
	}

//	private static void getOddsFor(List<Competition> competitions2) throws IOException {
//
//		String url = ServerConstants.GET_ODDS_FOR_DATES_URL + ServerConstants.API_FOOTBALL_KEY;
//		
//		Calendar calendar = Calendar.getInstance();
//		Date today = calendar.getTime();
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");  
//		String todayString = dateFormat.format(today);
//		
//		calendar.add(Calendar.DAY_OF_MONTH, 2);
//		String endDayString = dateFormat.format(calendar.getTime());
//		
//		url = url.replace(ServerConstants.REPLACE_DATE_FROM, todayString).replace(ServerConstants.REPLACE_DATE_TO, endDayString);
//		
////		String content = new HttpHelper().fetchGetContent(url);
////		Gson gson = new Gson();
////		List<Odd> odds = gson.fromJson(content, new TypeToken<List<Odd>>() {}.getType());
//		
//		Odd odd = new Odd();
//		odd.setOdd1("1,5");
//		odd.setOdd2("4");
//		odd.setOddX("3,8");
//		
//		for (Competition competition : competitions2) {
//			
//			if (competition.getEvents()==null){
//				continue;//TODO: why?
//			}
//			
//			for (Event event : competition.getEvents()) {
////				for (Odd odd : odds){
////				
////				if (event.getMatchId().equals(odd.getMatchId())){
//					event.setOdd(odd);
////				}
////					
////				}
//			}
//		}
//
//	}
	
}
