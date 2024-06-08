package gr.server.data.api.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.events.MatchEventStatistics;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Section;

public class FootballApiCache {
	
	/**
	 * Priorities for some major leagues.
	 */
	public static Map<Integer, Integer> PRIORITIES_OVERRIDDE = new HashMap<>();
	
	
	
	static int USA_MLS = 99; 
	static int NETHERLANDS_EREDIVISIE = 133; 
	static int RUSSIA_PREMIER_LEAGUE = 220; 
	public static int SPAIN_LA_LIGA = 251; 
	static int TURKEY_SUPER_LIG = 284; 
	static int SWITZERLAND_SUPER_LEAGUE = 279; 
	public static int ENGLAND_PREMIER_LEAGUE = 317; 
	static int ENGLAND_FA_CUP = 318; 
	static int ENGLAND_COMMUNITY_SHIELD = 319; 
	static int ENGLAND_EFL_CUP = 320; 
	static int ENGLAND_LEAGUE_TROPHY = 322; 
	static int AUSTRIA_BUNDESLIGA = 402; 
	static int DENMARK_SUPERLIGA = 463; 
	static int FRANCE_LIGUE_1 = 498; 
	static int FRANCE_COUPE_DE_LA_LIGUE = 499; 
	static int FRANCE_COUPE_DE_FRANCE = 500; 
	static int GERMANY_SUPER_CUP = 511; 
	static int GERMANY_BUNDESLIGA = 512; 
	static int GERMANY_DFP_POKAL = 513; 
	static int GREECE_SUPER_LEAGUE = 523; 
	static int GREECE_GREEK_CUP = 525; 
	static int ITALY_SERIE_A = 592; 
	static int ITALY_COPPA_ITALIA = 593; 
	static int ITALY_SUPERCOPA = 594; 
	static int SCOTLAND_PREMIERSHIP = 688; 
	static int SCOTLAND_LEAGUE_CUP = 693; 
	static int WORLD_CUP_QUAL_ASIA = 700; 
	static int WORLD_CUP_QUAL_SOUTH_AMERICA = 804; 
	static int WORLD_CUP_QUAL_OCEANIA = 814; 
	static int WORLD_CUP = 726; 
	static int WORLD_CONFEDERATIONS_CUP = 735; 
	static int OLYMPIC_GAMES = 731; 
	static int COPA_AMERICA = 803; 
	static int COPA_LIBERTADORES = 805; 
	static int COPA_SUDAMERICANA = 806; 
	static int UEFA_SUPER_CUP = 816; 
	static int UEFA_CHAMPIONS_LEAGUE= 817; 
	static int UEFA_EUROPA_LEAGUE= 818; 
	static int EUROPE_EURO_QUAL = 819; 
	static int EUROPE_EURO_CUP = 846; 

	static {
		PRIORITIES_OVERRIDDE.put(EUROPE_EURO_QUAL, Integer.MAX_VALUE-1);
		PRIORITIES_OVERRIDDE.put(UEFA_CHAMPIONS_LEAGUE, Integer.MAX_VALUE);
		PRIORITIES_OVERRIDDE.put(UEFA_EUROPA_LEAGUE, Integer.MAX_VALUE - 1);
		PRIORITIES_OVERRIDDE.put(WORLD_CUP, Integer.MAX_VALUE);
		PRIORITIES_OVERRIDDE.put(ENGLAND_PREMIER_LEAGUE, Integer.MAX_VALUE);
		PRIORITIES_OVERRIDDE.put(ITALY_SERIE_A, Integer.MAX_VALUE -2);
		PRIORITIES_OVERRIDDE.put(GERMANY_BUNDESLIGA, Integer.MAX_VALUE -2);
		PRIORITIES_OVERRIDDE.put(SPAIN_LA_LIGA, Integer.MAX_VALUE -1);
	};
	
	/**
	 * All the available sections. e.g. World is a section, Greece is a section etc.
	 */
	public static Map<Integer, Section> SECTIONS = new HashMap<>();
	
	/**
	 * All the leagues in the system. Every league belongs to a section. e.g. Club Friendlies belong to World section.
	 */
	public static Map<Integer, League> LEAGUES = new HashMap<>();
	
	/**
	 * All the standings for all leagues in the system. Every league belongs to a section. e.g. Club Friendlies belong to World section.
	 */
	public static Map<Integer, Season> STANDINGS = new HashMap<>();
	
	/**
	 * Maps days with the leagues and their games.
	 * Key '0' is considered to be today, '1' is tomorrow, '-1' is yesterday etc.
	 */
	public static Map<Integer, Map<League, Map<Integer, MatchEvent>>> EVENTS_PER_DAY_PER_LEAGUE = 
			new ConcurrentHashMap<Integer, Map<League,Map<Integer,MatchEvent>>>(3);
	
	public static Map<Integer, MatchEventStatistics> STATS_PER_EVENT = new HashMap<>();
	
	public static Map<Integer, MatchEventIncidents> INCIDENTS_PER_EVENT = new HashMap<>();
	
	/**
	 * 
	 */
	public static Map<Integer, MatchEvent> ALL_EVENTS = new HashMap<>();

}
