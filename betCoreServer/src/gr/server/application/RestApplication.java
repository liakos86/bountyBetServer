package gr.server.application;

import gr.server.impl.service.MyBetOddsServiceImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


/**
 * Imports the classes that will provide the rest service.
 */
public class RestApplication  extends Application
{
	/**
	 * Singleton for the mongo client.
	 */
	private static MongoClient mongoClient;
	
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
    
    /**
	 * mongoClient is a Singleton.
	 * We make sure here.
	 * 
	 * 
	 * @return
	 */
	public static  MongoClient getMongoClient() {
		if (mongoClient == null){
			mongoClient = connect();
		}
		return mongoClient;
	}

	private static MongoClient connect(){
		MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27000"));
		return mongoClient;
	}
   
}
