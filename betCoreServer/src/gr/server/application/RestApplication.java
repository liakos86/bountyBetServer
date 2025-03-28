package gr.server.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import gr.server.impl.service.MyBetOddsServiceImpl;


/**
 * Imports the classes that will provide the rest service.
 */
@ApplicationPath("/")
public class RestApplication  
extends Application{

//	/**
//	 * 
//	 */
//	private static SoccerEventsTopicProducer SOCCER_EVENTS_TOPIC_PRODUCER = new SoccerEventsTopicProducer();
//	
//	private static FireBaseConnectionHelper FIREBASE_TOPIC_PRODUCER = new FireBaseConnectionHelper();
//	
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<Class<?>>();
        resources.add(MyBetOddsServiceImpl.class);
        return resources;
    }
    
    @Override
    public Set<Object> getSingletons() {
    	return Collections.emptySet();
    }
    
//    public static void connectActiveMq() {
//    	SOCCER_EVENTS_TOPIC_PRODUCER.connect();
//    }
//    
//    public static void disconnectActiveMq() {
//		SOCCER_EVENTS_TOPIC_PRODUCER.disconnect();
//    }
//    
//    public static void sendTopicMessage(Map<String, Object> messageParams) {
//    	try {
//			SOCCER_EVENTS_TOPIC_PRODUCER.sendTopicMessage(messageParams);
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
//    }

//	public static void connectFirebase() {
//		FIREBASE_TOPIC_PRODUCER.connect();
//	}
//	
//	public static void sendFirebaseTopicMessage(Map<String, String> messageParams) {
//    	
//		FIREBASE_TOPIC_PRODUCER.sendTopicMessage(messageParams);
//		
//    }
//
//	public static void sendMockFirebaseTopicMessage() {
//		Map<String, String> msg = new HashMap<>();
//		msg.put("eventId", "1");
//		msg.put("changeEvent", "1");
//		msg.put("homeScore", "1");
//		msg.put("awayScore", "1");
////		RestApplication.sendTopicMessage(msg);
//
//		FIREBASE_TOPIC_PRODUCER.sendTopicMessage(msg);
//		
//	}
    
}
