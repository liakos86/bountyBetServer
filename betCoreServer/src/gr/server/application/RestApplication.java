package gr.server.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.impl.service.MyBetOddsServiceImpl;


/**
 * Imports the classes that will provide the rest service.
 */
@ApplicationPath("/")
public class RestApplication  
extends Application{

	/**
	 * Priorities for some major leagues.
	 */
	public static Map<Integer, Integer> PRIORITIES_OVERRIDDE = new HashMap<>();
	
	
	
	static int USA_MLS = 99; 
	static int NETHERLANDS_EREDIVISIE = 133; 
	static int RUSSIA_PREMIER_LEAGUE = 220; 
	static int SPAIN_LA_LIGA = 251; 
	static int TURKEY_SUPER_LIG = 284; 
	static int SWITZERLAND_SUPER_LEAGUE = 279; 
	static int ENGLAND_PREMIER_LEAGUE = 317; 
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
		PRIORITIES_OVERRIDDE.put(EUROPE_EURO_QUAL, Integer.MAX_VALUE);
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
	 * Maps days with the leagues and their games.
	 */
	public static Map<String, Map<League, Map<Integer, MatchEvent>>> EVENTS_PER_DAY_PER_LEAGUE = new LinkedHashMap<>();
	
	/**
	 * 
	 */
	public static Map<Integer, MatchEvent> ALL_EVENTS = new HashMap<>();
	
	/**
	 * 
	 */
	private static SoccerEventsTopicProducer SOCCER_EVENTS_TOPIC_PRODUCER = new SoccerEventsTopicProducer();
	
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<Class<?>>();
        System.out.println("REST configuration starting: getClasses()");      
        resources.add(MyBetOddsServiceImpl.class);
        System.out.println("REST configuration ended successfully.");
        return resources;
    }
    
    @Override
    public Set<Object> getSingletons() {
    	return Collections.emptySet();
    }
    
    public static void connectActiveMq() {
    	SOCCER_EVENTS_TOPIC_PRODUCER.connect();
    }
    
    public static void disconnectActiveMq() {
		SOCCER_EVENTS_TOPIC_PRODUCER.disconnect();
    }
    
    public static void sendTopicMessage(Map<String, Object> messageParams) {
    	try {
			SOCCER_EVENTS_TOPIC_PRODUCER.sendTopicMessage(messageParams);
		} catch (JMSException e) {
			e.printStackTrace();
		}
    }
    
}
