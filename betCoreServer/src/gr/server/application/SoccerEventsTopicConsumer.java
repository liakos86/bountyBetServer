//package gr.server.application;
//
//import javax.jms.Connection;
//import javax.jms.Destination;
//import javax.jms.ExceptionListener;
//import javax.jms.JMSException;
//import javax.jms.Message;
//import javax.jms.MessageConsumer;
//import javax.jms.MessageListener;
//import javax.jms.Session;
//
////import org.apache.activemq.ActiveMQConnectionFactory;
//
//import gr.server.data.constants.ActiveMqConstants;
//import gr.server.data.live.helper.LiveUpdatesHelper;
//
///**
// * 
// * This class connects to an ActiveMQ instance and creates a session bound to a specific topic,
// * the {@link ActiveMqConstants#TOPIC_SOCCER_EVENTS}.
// * {@link LiveUpdatesHelper} will send the events to be written to the topic.
// * 
// * @author liako
// *
// */
//public class SoccerEventsTopicConsumer 
//implements ExceptionListener, MessageListener {
//	
//	boolean connected = false;
//	
//	Session session;
//	
//	Connection connection;
//
//	MessageConsumer consumer;
//	
//	public SoccerEventsTopicConsumer(){
//		try {
//			connect();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void connect() throws Exception {
//		
//		try {
//			ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ActiveMqConstants.CONNECTION_TCP_URL);
//            connection = cf.createConnection();
//            connection.setExceptionListener(this);
//            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//            Destination destination = session.createTopic(ActiveMqConstants.TOPIC_SOCCER_EVENTS);
//		    consumer = session.createConsumer(destination);
//		    consumer.setMessageListener(this);
//		    connected = true;
//		    connection.start();
//		    System.out.println("*******************ACTIVEMQ CONSUMER OK");
//		} catch (JMSException e) {
//			connection.close();
//			connected = false;
//			throw new Exception(e.getMessage());
//		}catch (Exception e) {
//			connection.close();
//			connected = false;
//			throw new Exception(e.getMessage());
//		}finally {
//			
//		}
//		
//	}
//	
//	
//
//	@Override
//	public void onException(JMSException arg0) {
//		try {
//			System.out.println("ACTIVEMQ CONSUMER SHUT DOWN");
//
//			connection.close();
//		} catch (JMSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			connected = false;
//		}
//		
//	}
//
//
//
//	@Override
//	public void onMessage(Message message) {
//		try {
//			System.out.println("RECEIVED" + message.getObjectProperty("homeScore"));
//		} catch (JMSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//
//}
