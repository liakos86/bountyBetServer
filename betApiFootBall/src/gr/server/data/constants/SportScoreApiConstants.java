package gr.server.data.constants;

public interface SportScoreApiConstants {
	
	String MATCH_START_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";//2022-10-13 15:00:00
	
	String RAPID_API_HEADER_KEY = "X-RapidAPI-Key";
	
	String RAPID_API_HEADER_VALUE = "515a19444amsh5c64c4c7936b110p18da20jsnb78c0b6bdd4b";
	
	String RAPID_API_HEADER_HOST_KEY = "X-RapidAPI-Host";
	
	String RAPID_API_HEADER_HOST_VALUE = "sportscore1.p.rapidapi.com";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	String GET_EVENTS_BY_SPORT_DATE_URL = "https://sportscore1.p.rapidapi.com/sports/1/events/date/";//2022-08-30
	
	
	String GET_LEAGUE_BY_ID_URL = "https://sportscore1.p.rapidapi.com/leagues/";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	//String GET_TEAMS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/teams";

	//String GET_LEAGUES_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/leagues";

//	String GET_LIVE_EVENTS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/live";
	String GET_LIVE_EVENTS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/events/live";
	
	String GET_EVENT_INCIDENTS_URL = "https://sportscore1.p.rapidapi.com/events/_REPLACE_/incidents";
	
}
