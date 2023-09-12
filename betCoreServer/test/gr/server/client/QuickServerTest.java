package gr.server.client;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.junit.Test;

import gr.server.application.RestApplication;
import gr.server.application.SoccerEventsTopicConsumer;
import gr.server.application.SoccerEventsTopicProducer;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.Score;

public class QuickServerTest {
	
	//@Test
	public void test() throws JMSException, InterruptedException {
		SoccerEventsTopicProducer producer = new SoccerEventsTopicProducer();
		producer.connect();
		Thread.sleep(3000);
		Map<String, Object> msg = new HashMap<>();
		msg.put("eventId", 2055338);
		msg.put("changeEvent", ChangeEvent.AWAY_GOAL);
		Score homescore = new Score();;
		homescore.setCurrent(1);
		homescore.setDisplay(1);
		homescore.setPeriod_1(1);
		homescore.setPeriod_2(1);
		msg.put("homeScore", homescore );
		msg.put("awayScore", homescore);
		//RestApplication.sendTopicMessage(msg);
		
		Thread.sleep(5000);
		
		SoccerEventsTopicConsumer consumer = new SoccerEventsTopicConsumer();
		
		
		//Thread.sleep(2000);

		producer.sendTopicMessage(msg);
		
		
		//Thread.sleep(20000);
	}

}
