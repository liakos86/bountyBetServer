package gr.server.data.constants;

public interface SportScoreApiConstants {
	
	String SOCKET_CONN_URL = "wss://tipsscore.com:2083/app/7UXH2sNFqpVAO6FebyTKpujgfy8BUnM?protocol=7&client=js&version=5.0.3&flash=false";
	
	String SOCKET_BOOTSTRAP_MSG = "{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"en-football-list\"}}";
	
	String SOCKET_KEEP_ALIVE_MSG = "{\"event\":\"pusher:ping\",\"data\":{}}";
	
	Double STARTING_BALANCE = 1000d;
	
	String MATCH_START_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";//2022-10-13 15:00:00
	
	String GET_EVENTS_DATE_FORMAT = "yyyy-MM-dd";
	
	String RAPID_API_HEADER_KEY = "X-RapidAPI-Key";
	
	String RAPID_API_HEADER_VALUE = "515a19444amsh5c64c4c7936b110p18da20jsnb78c0b6bdd4b";
	
//	String RAPID_API_HEADER_VALUE = "85e030da0emshab164327ab5b045p1af3c4jsn649b7c8be9fd";
	
	String RAPID_API_HEADER_HOST_KEY = "X-RapidAPI-Host";
	
	String RAPID_API_HEADER_HOST_VALUE = "sportscore1.p.rapidapi.com";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	String GET_EVENTS_BY_SPORT_DATE_URL = "https://sportscore1.p.rapidapi.com/sports/1/events/date/";											
	
	
	String GET_LEAGUE_BY_ID_URL = "https://sportscore1.p.rapidapi.com/leagues/";
	
	String GET_SECTIONS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/sections?page=";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	//String GET_TEAMS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/teams";

	//String GET_LEAGUES_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/leagues";

	String GET_LIVE_EVENTS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/events/live";
	
	String GET_EVENT_INCIDENTS_URL = "https://sportscore1.p.rapidapi.com/events/_REPLACE_/incidents";
	
}
