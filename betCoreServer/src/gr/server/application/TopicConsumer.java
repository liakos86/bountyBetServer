package gr.server.application;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicConsumer {
	
	
	public static void main(String[]args) throws Exception {
		Connection connection = null;
		try {
			
			
			ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");

			// Create a Connection
            connection = cf.createConnection();
            connection.start();

            //connection.setExceptionListener(this);

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createTopic("Soccer.MatchEventsLive");

            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);

            // Wait for a message
           // Message message = consumer.receive(100000);
            
		    MessageListener listner = new MessageListener() {
	            public void onMessage(Message message) {
	                try {
	                    if (message instanceof TextMessage) {
	                        TextMessage textMessage = (TextMessage) message;
	                        System.out.println("Received message"
	                                + textMessage.getText() + "'");
	                    }
	                } catch (JMSException e) {
	                    System.out.println("Caught:" + e);
	                    e.printStackTrace();
	                }
	            }
	        };

	        consumer.setMessageListener(listner);
	        Thread.sleep(1000000);
		} catch (JMSException e) {
//			System.out.println(e.getMessage());
			throw new Exception(e.getMessage());
		}catch (Exception e) {
			throw new Exception(e.getMessage());
		}finally {
			connection.close();
		}
		
		System.out.println("CONSUME SUCCESS");
		
		
		
		
	}
	
}
