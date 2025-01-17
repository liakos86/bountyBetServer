package gr.server.data.api.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


import gr.server.common.util.ConcurrentLRUCache;

//import org.apache.activemq.util.LRUCache;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.user.model.objects.User;
import gr.server.impl.client.MongoClientHelperImpl;

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
	public static Map<Integer, Section> ALL_SECTIONS = new ConcurrentHashMap<>();
	
	/**
	 * All the leagues in the system. Every league belongs to a section. e.g. Club Friendlies belong to World section.
	 */
	public static Map<Integer, LeagueWithData> ALL_LEAGUES_WITH_EVENTS = new ConcurrentHashMap<>();

	public static Map<Integer, League> ALL_LEAGUES = new ConcurrentHashMap<>();

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
	
	public static Map<Integer, List<Integer>> SUPPORTED_SECTION_IDS = new HashMap<>();
	
//	public static Set<Integer> SUPPORTED_LEAGUE_IDS = new HashSet<>();
	
	public static Set<Integer> SETTLED_EVENTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	public static BlockingQueue<MatchEvent> FINISHED_EVENTS = new LinkedBlockingQueue<MatchEvent>();

	public static BlockingQueue<MatchEvent> WITHDRAWN_EVENTS = new LinkedBlockingQueue<MatchEvent>();
  	
	/**
	 * Stats will be fetched periodically. 
	 * Least recently used ones will be discarded.
	 */
//	public static LRUCache<Integer, MatchEventIncidentsWithStatistics> ALL_MATCH_STATS = new LRUCache<>(512);
	public static ConcurrentLRUCache<Integer, MatchEventIncidentsWithStatistics> ALL_MATCH_STATS = new ConcurrentLRUCache<>(256);
	
	static {
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(-1, new ArrayList<>());
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(0, new ArrayList<>());
		ALL_LEAGUES_WITH_EVENTS_PER_DAY.put(1, new ArrayList<>());
		
		SUPPORTED_SECTION_IDS.put(1, new ArrayList<>());//ireland
		SUPPORTED_SECTION_IDS.get(1).add(1);
		SUPPORTED_SECTION_IDS.get(1).add(2);
		SUPPORTED_SECTION_IDS.get(1).add(3);
		SUPPORTED_SECTION_IDS.get(1).add(4);
		
		SUPPORTED_SECTION_IDS.put(2, new ArrayList<>());//wales
		SUPPORTED_SECTION_IDS.get(2).add(5);
		SUPPORTED_SECTION_IDS.get(2).add(6);
		
		
		SUPPORTED_SECTION_IDS.put(9, new ArrayList<>());//norway
		SUPPORTED_SECTION_IDS.get(9).add(70);
		SUPPORTED_SECTION_IDS.get(9).add(71);
		SUPPORTED_SECTION_IDS.get(9).add(72);
		SUPPORTED_SECTION_IDS.get(9).add(75);
		
		
		SUPPORTED_SECTION_IDS.put(13, new ArrayList<>());//usa
		SUPPORTED_SECTION_IDS.get(13).add(99);
		SUPPORTED_SECTION_IDS.get(13).add(100);
		
		
		SUPPORTED_SECTION_IDS.put(18, new ArrayList<>());//nether
		SUPPORTED_SECTION_IDS.get(18).add(133);
		SUPPORTED_SECTION_IDS.get(18).add(134);
		SUPPORTED_SECTION_IDS.get(18).add(135);
		SUPPORTED_SECTION_IDS.get(18).add(136);
		
		
		SUPPORTED_SECTION_IDS.put(19, new ArrayList<>());//chech
		SUPPORTED_SECTION_IDS.get(19).add(140);
		SUPPORTED_SECTION_IDS.get(19).add(141);
		SUPPORTED_SECTION_IDS.get(19).add(142);
		SUPPORTED_SECTION_IDS.get(19).add(143);
		
		
		SUPPORTED_SECTION_IDS.put(21, new ArrayList<>());//argent
		SUPPORTED_SECTION_IDS.get(21).add(154);
		SUPPORTED_SECTION_IDS.get(21).add(156);
		SUPPORTED_SECTION_IDS.get(21).add(159);
		
		
		SUPPORTED_SECTION_IDS.put(22, new ArrayList<>());//austral
		SUPPORTED_SECTION_IDS.get(22).add(165);
		SUPPORTED_SECTION_IDS.get(22).add(173);
		
		
		
		SUPPORTED_SECTION_IDS.put(24, new ArrayList<>());//poland
		SUPPORTED_SECTION_IDS.get(24).add(191);
		SUPPORTED_SECTION_IDS.get(24).add(193);
		SUPPORTED_SECTION_IDS.get(24).add(194);
		
		
		SUPPORTED_SECTION_IDS.put(25, new ArrayList<>());//portug
		SUPPORTED_SECTION_IDS.get(25).add(203);
		SUPPORTED_SECTION_IDS.get(25).add(205);
		SUPPORTED_SECTION_IDS.get(25).add(206);
		SUPPORTED_SECTION_IDS.get(25).add(207);
		
		
		
		SUPPORTED_SECTION_IDS.put(26, new ArrayList<>());//rom
		SUPPORTED_SECTION_IDS.get(26).add(214);
		SUPPORTED_SECTION_IDS.get(26).add(215);
		SUPPORTED_SECTION_IDS.get(26).add(218);
		SUPPORTED_SECTION_IDS.get(26).add(219);
		
		
		SUPPORTED_SECTION_IDS.put(27, new ArrayList<>());//russ
		SUPPORTED_SECTION_IDS.get(27).add(220);
		SUPPORTED_SECTION_IDS.get(27).add(222);
		SUPPORTED_SECTION_IDS.get(27).add(230);
		
		
		SUPPORTED_SECTION_IDS.put(30, new ArrayList<>());//slovak
		SUPPORTED_SECTION_IDS.get(30).add(240);
		SUPPORTED_SECTION_IDS.get(30).add(242);
		SUPPORTED_SECTION_IDS.get(30).add(243);
		
		
		SUPPORTED_SECTION_IDS.put(31, new ArrayList<>());//sloven
		SUPPORTED_SECTION_IDS.get(31).add(246);
		SUPPORTED_SECTION_IDS.get(31).add(247);
		SUPPORTED_SECTION_IDS.get(31).add(248);
		
		
		SUPPORTED_SECTION_IDS.put(32, new ArrayList<>());//spain
		SUPPORTED_SECTION_IDS.get(32).add(250);
		SUPPORTED_SECTION_IDS.get(32).add(251);
		SUPPORTED_SECTION_IDS.get(32).add(252);
		SUPPORTED_SECTION_IDS.get(32).add(256);
		SUPPORTED_SECTION_IDS.get(32).add(257);
		SUPPORTED_SECTION_IDS.get(32).add(258);
		
		
		
		SUPPORTED_SECTION_IDS.put(33, new ArrayList<>());//sweden
		SUPPORTED_SECTION_IDS.get(33).add(260);
		SUPPORTED_SECTION_IDS.get(33).add(261);
		SUPPORTED_SECTION_IDS.get(33).add(270);
		SUPPORTED_SECTION_IDS.get(33).add(273);
		
		
		
		
		SUPPORTED_SECTION_IDS.put(34, new ArrayList<>());//swiss
		SUPPORTED_SECTION_IDS.get(34).add(279);
		SUPPORTED_SECTION_IDS.get(34).add(281);
		
		
		
		SUPPORTED_SECTION_IDS.put(35, new ArrayList<>());//turk
		SUPPORTED_SECTION_IDS.get(35).add(284);
		SUPPORTED_SECTION_IDS.get(35).add(285);
		SUPPORTED_SECTION_IDS.get(35).add(288);
		SUPPORTED_SECTION_IDS.get(35).add(289);
		
		
		SUPPORTED_SECTION_IDS.put(36, new ArrayList<>());//uckra
		SUPPORTED_SECTION_IDS.get(36).add(295);
		SUPPORTED_SECTION_IDS.get(36).add(296);
		SUPPORTED_SECTION_IDS.get(36).add(297);
		
		
		SUPPORTED_SECTION_IDS.put(37, new ArrayList<>());//urug
		SUPPORTED_SECTION_IDS.get(36).add(302);
		SUPPORTED_SECTION_IDS.get(36).add(303);
		SUPPORTED_SECTION_IDS.get(36).add(305);
		SUPPORTED_SECTION_IDS.get(36).add(306);
		
		
		SUPPORTED_SECTION_IDS.put(38, new ArrayList<>());//bosn
		SUPPORTED_SECTION_IDS.get(38).add(307);
		SUPPORTED_SECTION_IDS.get(38).add(308);
		
		
		SUPPORTED_SECTION_IDS.put(40, new ArrayList<>());//engl
		SUPPORTED_SECTION_IDS.get(40).add(317);
		SUPPORTED_SECTION_IDS.get(40).add(318);
		SUPPORTED_SECTION_IDS.get(40).add(319);
		SUPPORTED_SECTION_IDS.get(40).add(320);
		SUPPORTED_SECTION_IDS.get(40).add(321);
		SUPPORTED_SECTION_IDS.get(40).add(322);
		SUPPORTED_SECTION_IDS.get(40).add(326);
		SUPPORTED_SECTION_IDS.get(40).add(327);
		
		SUPPORTED_SECTION_IDS.put(86, new ArrayList<>());//france
		SUPPORTED_SECTION_IDS.get(86).add(498);
		SUPPORTED_SECTION_IDS.get(86).add(499);
		SUPPORTED_SECTION_IDS.get(86).add(500);
		SUPPORTED_SECTION_IDS.get(86).add(501);
		SUPPORTED_SECTION_IDS.get(86).add(503);
		
		
		
		SUPPORTED_SECTION_IDS.put(91, new ArrayList<>());//greece
		SUPPORTED_SECTION_IDS.get(91).add(523);
		SUPPORTED_SECTION_IDS.get(91).add(525);
		
		
		SUPPORTED_SECTION_IDS.put(101, new ArrayList<>());//italy
		SUPPORTED_SECTION_IDS.get(101).add(592);
		SUPPORTED_SECTION_IDS.get(101).add(593);
		SUPPORTED_SECTION_IDS.get(101).add(594);
		SUPPORTED_SECTION_IDS.get(101).add(595);
		

		
		SUPPORTED_SECTION_IDS.put(43, new ArrayList<>());//alb
		SUPPORTED_SECTION_IDS.get(43).add(340);
		SUPPORTED_SECTION_IDS.get(43).add(342);
		SUPPORTED_SECTION_IDS.get(43).add(343);
		
		SUPPORTED_SECTION_IDS.put(44, new ArrayList<>());//serbia
		SUPPORTED_SECTION_IDS.get(44).add(344);
		SUPPORTED_SECTION_IDS.get(44).add(345);
		
		
		SUPPORTED_SECTION_IDS.put(46, new ArrayList<>());//monten
		SUPPORTED_SECTION_IDS.get(46).add(360);
		
		
		SUPPORTED_SECTION_IDS.put(55, new ArrayList<>());//uae
		SUPPORTED_SECTION_IDS.get(55).add(376);
		SUPPORTED_SECTION_IDS.get(55).add(377);
		SUPPORTED_SECTION_IDS.get(55).add(378);
		SUPPORTED_SECTION_IDS.get(55).add(379);
		
		
		SUPPORTED_SECTION_IDS.put(65, new ArrayList<>());//austri
		SUPPORTED_SECTION_IDS.get(65).add(402);
		SUPPORTED_SECTION_IDS.get(65).add(404);
		
		
		SUPPORTED_SECTION_IDS.put(69, new ArrayList<>());//belg
		SUPPORTED_SECTION_IDS.get(69).add(416);
		SUPPORTED_SECTION_IDS.get(69).add(417);
		SUPPORTED_SECTION_IDS.get(69).add(418);
		
		
		SUPPORTED_SECTION_IDS.put(71, new ArrayList<>());//bulg
		SUPPORTED_SECTION_IDS.get(71).add(425);
		SUPPORTED_SECTION_IDS.get(71).add(426);
		SUPPORTED_SECTION_IDS.get(71).add(427);
		
		
		SUPPORTED_SECTION_IDS.put(75, new ArrayList<>());//china
		SUPPORTED_SECTION_IDS.get(75).add(442);
		SUPPORTED_SECTION_IDS.get(75).add(444);
		SUPPORTED_SECTION_IDS.get(75).add(445);
		
		
		SUPPORTED_SECTION_IDS.put(77, new ArrayList<>());//croa
		SUPPORTED_SECTION_IDS.get(77).add(454);
		SUPPORTED_SECTION_IDS.get(77).add(455);
		SUPPORTED_SECTION_IDS.get(77).add(457);
		
		
		SUPPORTED_SECTION_IDS.put(78, new ArrayList<>());//cypr
		SUPPORTED_SECTION_IDS.get(78).add(458);
		SUPPORTED_SECTION_IDS.get(78).add(459);
		SUPPORTED_SECTION_IDS.get(78).add(460);
		
		
		SUPPORTED_SECTION_IDS.put(79, new ArrayList<>());//denm
		SUPPORTED_SECTION_IDS.get(79).add(463);
		SUPPORTED_SECTION_IDS.get(79).add(466);
		
		
		SUPPORTED_SECTION_IDS.put(81, new ArrayList<>());//egypt
		SUPPORTED_SECTION_IDS.get(81).add(472);
		SUPPORTED_SECTION_IDS.get(81).add(473);
		SUPPORTED_SECTION_IDS.get(81).add(475);
		
		SUPPORTED_SECTION_IDS.put(85, new ArrayList<>());//finl
		SUPPORTED_SECTION_IDS.get(85).add(487);
		SUPPORTED_SECTION_IDS.get(85).add(489);
		SUPPORTED_SECTION_IDS.get(85).add(493);
		
		
		SUPPORTED_SECTION_IDS.put(94, new ArrayList<>());//hung
		SUPPORTED_SECTION_IDS.get(94).add(545);
		SUPPORTED_SECTION_IDS.get(94).add(546);
		SUPPORTED_SECTION_IDS.get(94).add(547);
		SUPPORTED_SECTION_IDS.get(94).add(548);
		
		
		
		SUPPORTED_SECTION_IDS.put(95, new ArrayList<>());//iceland
		SUPPORTED_SECTION_IDS.get(95).add(553);
		SUPPORTED_SECTION_IDS.get(95).add(554);
		SUPPORTED_SECTION_IDS.get(95).add(556);
		SUPPORTED_SECTION_IDS.get(95).add(557);
		
		
		SUPPORTED_SECTION_IDS.put(99, new ArrayList<>());//ireland
		SUPPORTED_SECTION_IDS.get(99).add(576);
		SUPPORTED_SECTION_IDS.get(99).add(578);
		SUPPORTED_SECTION_IDS.get(99).add(579);
		
		
		SUPPORTED_SECTION_IDS.put(100, new ArrayList<>());//israel
		SUPPORTED_SECTION_IDS.get(100).add(582);
		
		
		SUPPORTED_SECTION_IDS.put(103, new ArrayList<>());//japan
		SUPPORTED_SECTION_IDS.get(103).add(627);
		SUPPORTED_SECTION_IDS.get(103).add(628);
		SUPPORTED_SECTION_IDS.get(103).add(629);
		SUPPORTED_SECTION_IDS.get(103).add(630);
		
		
		SUPPORTED_SECTION_IDS.put(108, new ArrayList<>());//latvia
		SUPPORTED_SECTION_IDS.get(108).add(649);
		
		
		SUPPORTED_SECTION_IDS.put(111, new ArrayList<>());//lith
		SUPPORTED_SECTION_IDS.get(111).add(658);
		SUPPORTED_SECTION_IDS.get(111).add(660);
		SUPPORTED_SECTION_IDS.get(111).add(661);
		
		
		SUPPORTED_SECTION_IDS.put(115, new ArrayList<>());//mexico
		SUPPORTED_SECTION_IDS.get(115).add(673);
		SUPPORTED_SECTION_IDS.get(115).add(674);
		SUPPORTED_SECTION_IDS.get(115).add(678);
		
		
		SUPPORTED_SECTION_IDS.put(117, new ArrayList<>());//scot
		SUPPORTED_SECTION_IDS.get(117).add(688);
		SUPPORTED_SECTION_IDS.get(117).add(692);
		SUPPORTED_SECTION_IDS.get(117).add(693);
		SUPPORTED_SECTION_IDS.get(117).add(694);
		
		
		SUPPORTED_SECTION_IDS.put(118, new ArrayList<>());//asia
		SUPPORTED_SECTION_IDS.get(118).add(699);
		SUPPORTED_SECTION_IDS.get(118).add(700);
		SUPPORTED_SECTION_IDS.get(118).add(703);
		
		
		SUPPORTED_SECTION_IDS.put(119, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(119).add(726);
		SUPPORTED_SECTION_IDS.get(119).add(731);
		SUPPORTED_SECTION_IDS.get(119).add(732);
		SUPPORTED_SECTION_IDS.get(119).add(734);
		SUPPORTED_SECTION_IDS.get(119).add(735);
		SUPPORTED_SECTION_IDS.get(119).add(764);
		SUPPORTED_SECTION_IDS.get(119).add(766);
		SUPPORTED_SECTION_IDS.get(119).add(769);
		SUPPORTED_SECTION_IDS.get(119).add(776);
		SUPPORTED_SECTION_IDS.get(119).add(780);
		SUPPORTED_SECTION_IDS.get(119).add(781);
		SUPPORTED_SECTION_IDS.get(119).add(782);
		SUPPORTED_SECTION_IDS.get(119).add(8713);
		SUPPORTED_SECTION_IDS.get(119).add(8702);
		
		
		SUPPORTED_SECTION_IDS.put(120, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(120).add(783);
		SUPPORTED_SECTION_IDS.get(120).add(786);
		SUPPORTED_SECTION_IDS.get(120).add(797);
		
		SUPPORTED_SECTION_IDS.put(121, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(121).add(803);
		SUPPORTED_SECTION_IDS.get(121).add(804);
		SUPPORTED_SECTION_IDS.get(121).add(805);
		SUPPORTED_SECTION_IDS.get(121).add(806);
		SUPPORTED_SECTION_IDS.get(121).add(807);
		
		
		SUPPORTED_SECTION_IDS.put(122, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(122).add(814);
		SUPPORTED_SECTION_IDS.get(122).add(815);
		
		
		SUPPORTED_SECTION_IDS.put(123, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(123).add(816);
		SUPPORTED_SECTION_IDS.get(123).add(817);
		SUPPORTED_SECTION_IDS.get(123).add(818);
		SUPPORTED_SECTION_IDS.get(123).add(819);
		SUPPORTED_SECTION_IDS.get(123).add(821);
		SUPPORTED_SECTION_IDS.get(123).add(822);
		SUPPORTED_SECTION_IDS.get(123).add(823);
		SUPPORTED_SECTION_IDS.get(123).add(839);
		SUPPORTED_SECTION_IDS.get(123).add(845);
		SUPPORTED_SECTION_IDS.get(123).add(846);
		SUPPORTED_SECTION_IDS.get(123).add(848);
		
		
		SUPPORTED_SECTION_IDS.put(125, new ArrayList<>());//world
		SUPPORTED_SECTION_IDS.get(125).add(869);
		SUPPORTED_SECTION_IDS.get(125).add(870);
		SUPPORTED_SECTION_IDS.get(125).add(871);
		SUPPORTED_SECTION_IDS.get(125).add(873);
		SUPPORTED_SECTION_IDS.get(125).add(874);
		SUPPORTED_SECTION_IDS.get(125).add(875);
		SUPPORTED_SECTION_IDS.get(125).add(879);
		
		
		SUPPORTED_SECTION_IDS.put(134, new ArrayList<>());//noth mac
		SUPPORTED_SECTION_IDS.get(134).add(889);
		SUPPORTED_SECTION_IDS.get(134).add(890);
		SUPPORTED_SECTION_IDS.get(134).add(892);
		
		
		SUPPORTED_SECTION_IDS.put(135, new ArrayList<>());//brazil
		SUPPORTED_SECTION_IDS.get(135).add(893);
		SUPPORTED_SECTION_IDS.get(135).add(894);
		SUPPORTED_SECTION_IDS.get(135).add(896);
		
		
		SUPPORTED_SECTION_IDS.put(379, new ArrayList<>());//not cancelled ????
		
		
		SUPPORTED_SECTION_IDS.put(487, new ArrayList<>());//interna
		
		
		
		
		
		
	}

	public static void checkForCaching(MatchEvent incomingEvent) {
		if (!FootballApiCache.SETTLED_EVENTS.contains(incomingEvent.getId())
				&& MatchEventStatus.FINISHED.getStatusStr().equals(incomingEvent.getStatus())
				&& incomingEvent.getWinner_code() != 0
				&& !FootballApiCache.FINISHED_EVENTS.contains(incomingEvent)) {
			
				System.out.println("ADDING TO FINISHED " + incomingEvent);
				try {
					FootballApiCache.FINISHED_EVENTS.put(incomingEvent);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}else if( (MatchEventStatus.POSTPONED.getStatusStr().equals(incomingEvent.getStatus())
				|| MatchEventStatus.CANCELLED.getStatusStr().equals(incomingEvent.getStatus())
				|| MatchEventStatus.CANCELED.getStatusStr().equals(incomingEvent.getStatus())
				|| MatchEventStatus.SUSPENDED.getStatusStr().equals(incomingEvent.getStatus()))
				&& !FootballApiCache.WITHDRAWN_EVENTS.contains(incomingEvent)) {
			
			System.out.println("WITHDRAWING:::" + incomingEvent);
				try {
					FootballApiCache.WITHDRAWN_EVENTS.put(incomingEvent);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		
	}

}
