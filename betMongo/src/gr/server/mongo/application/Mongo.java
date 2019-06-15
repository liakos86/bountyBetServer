package gr.server.mongo.application;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;


/**
 * Imports the classes that will provide the rest service.
 */
public class Mongo
{
	/**
	 * Singleton for the mongo client.
	 */
	private static MongoClient mongoClient;
	
	private Mongo(){}
	
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
