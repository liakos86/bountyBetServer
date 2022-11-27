package gr.server.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.impl.service.MyBetOddsServiceImpl;
import gr.server.util.DateUtils;


/**
 * Imports the classes that will provide the rest service.
 */
@ApplicationPath("/")
public class RestApplication  extends Application
{
//	/**
//	 * Singleton for the mongo client.
//	 */
//	private static MongoClient mongoClient;
	
//	public static Map<Integer, Integer> LEAGUE_PRIORITIES;
	
	public static Map<Integer, Section> SECTIONS = new HashMap<>();
	
	public static Map<Integer, League> LEAGUES = new HashMap<>();
	
	public static Map<Integer, Integer> PRIORITY_PER_LEAGUE = new HashMap<>();

	public static Map<String, ChangeEvent> LIVE_CHANGES_PER_EVENT = new HashMap<>();
	
	public static Map<String, Map<League, Map<Integer, MatchEvent>>> EVENTS_PER_DAY_PER_LEAGUE = new HashMap<>();
	
	public static Map<League, Map<Integer, MatchEvent>> LIVE_EVENTS_PER_LEAGUE = new HashMap<>();
	
	static {
		EVENTS_PER_DAY_PER_LEAGUE.put(DateUtils.todayStr(), new HashMap<>());
		
//		LEAGUE_PRIORITIES.put(317, Integer.MIN_VALUE);
//		LEAGUE_PRIORITIES.put(251, Integer.MIN_VALUE+1);
//		LEAGUE_PRIORITIES.put(512, Integer.MIN_VALUE+2);
//		LEAGUE_PRIORITIES.put(592, Integer.MIN_VALUE+3);
//		LEAGUE_PRIORITIES.put(498, Integer.MIN_VALUE+4);
		
	}
	
	
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
    
//    /**
//	 * mongoClient is a Singleton.
//	 * We make sure here.
//	 * 
//	 * 
//	 * @return
//	 */
//	public static  MongoClient getMongoClient() {
//		if (mongoClient == null){
//			mongoClient = connect();
//		}
//		return mongoClient;
//	}
//
//	private static MongoClient connect(){
//		final String conn = "mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority";
//
//		MongoClientURI uri = new MongoClientURI(conn + "<dbname>?ssl=true&replicaSet=spearo-shard-0&authSource=admin&retryWrites=true&w=majority");
//		mongoClient = new MongoClient(uri);
//		//MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27000"));
//		return mongoClient;
//	}
   
}
