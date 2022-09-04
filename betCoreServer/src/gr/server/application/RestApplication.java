package gr.server.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Team;
import gr.server.data.user.model.objects.SettledEvent;
import gr.server.impl.service.MyBetOddsServiceImpl;


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
	
	public static List<MatchEvent> EVENTS = new ArrayList<MatchEvent>();
	
	public static List<Team> TEAMS_FOOTBALL = new ArrayList<Team>();
	
	public static List<SettledEvent> SETTLED = new ArrayList<SettledEvent>();
	
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
