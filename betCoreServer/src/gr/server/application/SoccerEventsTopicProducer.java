package gr.server.application;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.gson.Gson;

import gr.server.data.api.model.events.transients.ChangeEventSoccer;
import gr.server.data.constants.ActiveMqConstants;
import gr.server.data.live.helper.LiveUpdatesHelper;

/**
 * 
 * This class connects to an ActiveMQ instance and creates a session bound to a
 * specific topic, the {@link ActiveMqConstants#TOPIC_SOCCER_EVENTS}.
 * {@link LiveUpdatesHelper} will send the events to be written to the topic.
 * 
 * @author liako
 *
 */
public class SoccerEventsTopicProducer implements ExceptionListener {

	boolean connected = false;

	Session session;

	Connection connection;

	MessageProducer producer;


	public void connect() {
		try {
			_connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		if (!connected) {
			return;
		}
		
		try {
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}

		System.out.println("ACTIVE MQ DISCONNECTED");
	}

	private void _connect() throws Exception {

		try {
			ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(ActiveMqConstants.CONNECTION_TCP_URL);
			connection = cf.createConnection();
			connection.start();
			connection.setExceptionListener(this);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createTopic(ActiveMqConstants.TOPIC_SOCCER_EVENTS);
			producer = session.createProducer(destination);
			producer.setTimeToLive(2000);
			connected = true;
			System.out.println("*******************ACTIVEMQ ");
		} catch (JMSException e) {
			connection.close();
			connected = false;
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			connection.close();
			connected = false;
			throw new Exception(e.getMessage());
		} finally {

		}

	}

	public void sendTopicMessage(Map<String, Object> msg) throws JMSException {
		if (!connected || session == null) { // TODO investigate null
			connect();
		}
		
		TextMessage testMessage;
		try {
			testMessage = session.createTextMessage();
			ChangeEventSoccer changeEventSoccer = new ChangeEventSoccer(msg);
			String json = new Gson().toJson(changeEventSoccer);
			testMessage.setText(json);
			producer.send(testMessage);
			System.out.println("SENDING: " + changeEventSoccer.getChangeEvent());
		} catch (JMSException e) {
			connection.close();
			connected = false;
			e.printStackTrace();
		}
	}

	@Override
	public void onException(JMSException arg0) {
		try {
			System.out.println("ACTIVEMQ SHUT DOWN");
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connected = false;
		}

	}

}
