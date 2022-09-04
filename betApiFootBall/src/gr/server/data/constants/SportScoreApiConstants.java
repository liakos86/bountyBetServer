package gr.server.data.constants;

public interface SportScoreApiConstants {
	
	String RAPID_API_HEADER_KEY = "X-RapidAPI-Key";
	
	String RAPID_API_HEADER_VALUE = "515a19444amsh5c64c4c7936b110p18da20jsnb78c0b6bdd4b";
	
	String RAPID_API_HEADER_HOST_KEY = "X-RapidAPI-Host";
	
	String RAPID_API_HEADER_HOST_VALUE = "sportscore1.p.rapidapi.com";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	String GET_EVENTS_BY_SPORT_DATE_URL = "https://sportscore1.p.rapidapi.com/sports/1/events/date/2022-08-30";
	
	/**
	 * '1' is for football. we need to fetch sports to iterate.
	 */
	String GET_TEAMS_BY_SPORT_URL = "https://sportscore1.p.rapidapi.com/sports/1/teams";

}
