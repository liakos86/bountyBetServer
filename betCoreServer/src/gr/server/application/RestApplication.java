package gr.server.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.impl.service.MyBetOddsServiceImpl;
import gr.server.util.update.MinuteTracker;


/**
 * Imports the classes that will provide the rest service.
 */
@ApplicationPath("/")
public class RestApplication  
extends Application{

	/**
	 * Live games are inserted in this, in order to update their minute.
	 */
	public static MinuteTracker MINUTE_TRACKER = new MinuteTracker();
	
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
	 * The live events. The events in this must also be in MINUTE_TRACKER.
	 */
	public static Map<League, Map<Integer, MatchEvent>> LIVE_EVENTS_PER_LEAGUE = new HashMap<>();

	/**
	 * 
	 */
	public static Map<Integer, MatchEvent> ALL_EVENTS = new HashMap<>();
	
	
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
    
}
