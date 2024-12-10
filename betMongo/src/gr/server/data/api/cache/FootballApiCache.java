package gr.server.data.api.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.activemq.util.LRUCache;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.user.model.objects.User;

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
	static int UEFA_CONFERENCE_LEAGUE = 8911;

	static {
		PRIORITIES_OVERRIDDE.put(WORLD_CUP, Integer.MAX_VALUE);
		PRIORITIES_OVERRIDDE.put(EUROPE_EURO_QUAL, Integer.MAX_VALUE-1);
		
		PRIORITIES_OVERRIDDE.put(UEFA_CHAMPIONS_LEAGUE, Integer.MAX_VALUE - 2);
		PRIORITIES_OVERRIDDE.put(UEFA_EUROPA_LEAGUE, Integer.MAX_VALUE - 3);
		PRIORITIES_OVERRIDDE.put(UEFA_CONFERENCE_LEAGUE, Integer.MAX_VALUE - 4);
		PRIORITIES_OVERRIDDE.put(UEFA_SUPER_CUP, Integer.MAX_VALUE - 5);
		
		PRIORITIES_OVERRIDDE.put(ENGLAND_PREMIER_LEAGUE, Integer.MAX_VALUE - 6);
		PRIORITIES_OVERRIDDE.put(ITALY_SERIE_A, Integer.MAX_VALUE - 7);
		PRIORITIES_OVERRIDDE.put(GERMANY_BUNDESLIGA, Integer.MAX_VALUE - 8);
		PRIORITIES_OVERRIDDE.put(SPAIN_LA_LIGA, Integer.MAX_VALUE - 9);
		PRIORITIES_OVERRIDDE.put(FRANCE_LIGUE_1, Integer.MAX_VALUE - 10);

		
		PRIORITIES_OVERRIDDE.put(ENGLAND_FA_CUP, Integer.MAX_VALUE - 11);
		PRIORITIES_OVERRIDDE.put(ENGLAND_LEAGUE_TROPHY, Integer.MAX_VALUE - 12);
		PRIORITIES_OVERRIDDE.put(ENGLAND_EFL_CUP, Integer.MAX_VALUE - 13);
		PRIORITIES_OVERRIDDE.put(ENGLAND_COMMUNITY_SHIELD, Integer.MAX_VALUE - 14);
	};
	
	/**
	 * All the available sections. e.g. World is a section, Greece is a section etc.
	 */
	public static Map<Integer, Section> ALL_SECTIONS = new HashMap<>();
	
	/**
	 * All the leagues in the system. Every league belongs to a section. e.g. Club Friendlies belong to World section.
	 */
	public static Map<Integer, LeagueWithData> ALL_LEAGUES_WITH_EVENTS = new ConcurrentHashMap<>();

	public static Map<Integer, League> ALL_LEAGUES = new HashMap<>();

//	public static Map<Integer, User> LEADERBOARD = new HashMap<>();

	public static Map<Integer, Team> ALL_TEAMS = new HashMap<>();
	
	public static Map<Integer, List<LeagueWithData>> ALL_LEAGUES_WITH_EVENTS_PER_DAY = new LinkedHashMap<>(3);

	/**
	 * All the seasons for all leagues in the system. Every league belongs to a section. e.g. Club Friendlies belong to World section.
	 */
	public static Map<Integer, List<Season>> SEASONS_PER_LEAGUE = new ConcurrentHashMap<Integer, List<Season>>();

	/**
	 * 
	 */
	public static Map<Integer, MatchEvent> ALL_EVENTS = new ConcurrentHashMap<Integer, MatchEvent>();

	public static Map<Integer, List<User>> LEADERS = new ConcurrentHashMap<Integer, List<User>>();
	
	/**
	 * 
	 */
	public static Map<Integer, MatchEvent> LIVE_EVENTS = new ConcurrentHashMap<Integer, MatchEvent>();
	
	public static Set<Integer> SUPPORTED_SECTION_IDS = new HashSet<>();
	
	public static BlockingQueue<MatchEvent> FINISHED_EVENTS = new LinkedBlockingQueue<MatchEvent>();
  	
	/**
	 * Stats will be fetched periodically. 
	 * Least recently used ones will be discarded.
	 */
	public static LRUCache<Integer, MatchEventIncidentsWithStatistics> ALL_MATCH_STATS = new LRUCache<>(512);
	
	static {
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(-1, new ArrayList<>());
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(0, new ArrayList<>());
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(1, new ArrayList<>());
		
		SUPPORTED_SECTION_IDS.add(1);
		SUPPORTED_SECTION_IDS.add(2);
		SUPPORTED_SECTION_IDS.add(3);
		SUPPORTED_SECTION_IDS.add(4);
		SUPPORTED_SECTION_IDS.add(5);
		SUPPORTED_SECTION_IDS.add(7);
		SUPPORTED_SECTION_IDS.add(8);
		SUPPORTED_SECTION_IDS.add(9);
		SUPPORTED_SECTION_IDS.add(13);
		SUPPORTED_SECTION_IDS.add(18);
		SUPPORTED_SECTION_IDS.add(19);
		SUPPORTED_SECTION_IDS.add(21);
		SUPPORTED_SECTION_IDS.add(22);
		SUPPORTED_SECTION_IDS.add(24);
		SUPPORTED_SECTION_IDS.add(25);
		SUPPORTED_SECTION_IDS.add(26);
		SUPPORTED_SECTION_IDS.add(27);
		SUPPORTED_SECTION_IDS.add(30);
		SUPPORTED_SECTION_IDS.add(31);
		SUPPORTED_SECTION_IDS.add(32);
		SUPPORTED_SECTION_IDS.add(33);//sweden
		SUPPORTED_SECTION_IDS.add(34);//swiss
		SUPPORTED_SECTION_IDS.add(35);//turk
		SUPPORTED_SECTION_IDS.add(36);//uckra
		SUPPORTED_SECTION_IDS.add(37);//urug
		SUPPORTED_SECTION_IDS.add(38);//bosn
		SUPPORTED_SECTION_IDS.add(40);//engl
		SUPPORTED_SECTION_IDS.add(43);//alb
		SUPPORTED_SECTION_IDS.add(44);//serbia
		SUPPORTED_SECTION_IDS.add(46);//monten
		SUPPORTED_SECTION_IDS.add(52);//south af
		SUPPORTED_SECTION_IDS.add(55);//uae
		SUPPORTED_SECTION_IDS.add(65);//austr
		SUPPORTED_SECTION_IDS.add(67);//belar
		SUPPORTED_SECTION_IDS.add(69);//belg
		SUPPORTED_SECTION_IDS.add(71);//bulg
		SUPPORTED_SECTION_IDS.add(75);//china
		SUPPORTED_SECTION_IDS.add(77);//croa
		SUPPORTED_SECTION_IDS.add(78);//cypr
		SUPPORTED_SECTION_IDS.add(79);//denm
		SUPPORTED_SECTION_IDS.add(81);//egypt
		SUPPORTED_SECTION_IDS.add(85);//finl
		SUPPORTED_SECTION_IDS.add(86);//france
		SUPPORTED_SECTION_IDS.add(91);//greece
		SUPPORTED_SECTION_IDS.add(94);//hung
		SUPPORTED_SECTION_IDS.add(95);//iceland
		SUPPORTED_SECTION_IDS.add(99);//ireland
		SUPPORTED_SECTION_IDS.add(100);//israel
		SUPPORTED_SECTION_IDS.add(101);//italy
		SUPPORTED_SECTION_IDS.add(103);//japan
		SUPPORTED_SECTION_IDS.add(108);//latvia
		SUPPORTED_SECTION_IDS.add(111);//lith
		SUPPORTED_SECTION_IDS.add(115);//mexico
		SUPPORTED_SECTION_IDS.add(117);//scot
		SUPPORTED_SECTION_IDS.add(118);//asia
		SUPPORTED_SECTION_IDS.add(119);//world
		SUPPORTED_SECTION_IDS.add(120);//world
		SUPPORTED_SECTION_IDS.add(121);//world
		SUPPORTED_SECTION_IDS.add(122);//world
		SUPPORTED_SECTION_IDS.add(123);//world
		SUPPORTED_SECTION_IDS.add(125);//world
		SUPPORTED_SECTION_IDS.add(134);//noth mac
		SUPPORTED_SECTION_IDS.add(135);//brazil
		SUPPORTED_SECTION_IDS.add(379);//not cancelled ????
		SUPPORTED_SECTION_IDS.add(487);//interna
		
		
	}

}
