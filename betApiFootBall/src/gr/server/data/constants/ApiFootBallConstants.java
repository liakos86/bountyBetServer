package gr.server.data.constants;

public interface ApiFootBallConstants {
	
    public static final String EVENT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    
    public static final String AWARD_DATE_FORMAT = "yyyy-MM";
	
	public static final Double STARTING_BALANCE = 1000d;
	
	public static final String API_FOOTBALL_KEY = 
			"&APIkey=72c3d43de6a0fcb90618f00998a513b5dd824c8a71126c23dec2f95cfb3111af";//bountybet1
			
//			"&APIkey=1137079f2518afcc302025cd6e35dcf122297eac5660f8a506906140751ae3cd";

	public static final String GET_COUNTRIES_URL = "https://apifootball.com/api/?action=get_countries";
	
	public static final String GET_LEAGUES_FOR_COUNTRY_URL = "https://apifootball.com/api/?action=get_leagues&country_id=";
	
	public static final String GET_STANDINGS_FOR_LEAGUE_URL = "https://apifootball.com/api/?action=get_standings&league_id=";

	public static final String REPLACE_DATE_FROM = "DATE_FROM";

	public static final String REPLACE_DATE_TO = "DATE_TO";
	
	public static final String REPLACE_TEAM_1 = "TEAM_1";

	public static final String REPLACE_TEAM_2 = "TEAM_2";
	
	public static final String LIVE_MATCH = "match_live=1";
	
	public static final String GET_EVENTS_FOR_DATES = "https://apifootball.com/api/?action=get_events&from="+ REPLACE_DATE_FROM + "&to=" + REPLACE_DATE_TO ;
	
	public static final String LEAGUE_URL = "&league_id=";
	
	public static final String GET_ODDS_FOR_DATES_URL = "https://apifootball.com/api/?action=get_odds&from=" + REPLACE_DATE_FROM + "&to=" + REPLACE_DATE_TO;
	
	public static final String GET_HISTORY_BETWEEN_URL = "https://apifootball.com/api/?action=get_H2H&firstTeam=" + REPLACE_TEAM_1 + "&secondTeam=" + REPLACE_TEAM_2;

	public static final Long DAILY_INTERVAL = 1000*60*60*24L;
	
}
