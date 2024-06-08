package gr.server.application;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.Score;

public class TestFireBaseConnectionHelper {
	
	@Test
	public void test() throws Exception {
		FireBaseConnectionHelper helper = new FireBaseConnectionHelper();
		
		
		helper.connect();
		
		if (!helper.connected) {
			throw new Exception("Failed to connect");
		}
		
		
		Map<String, Object> params = new HashMap<>();
		
		params.put("changeEvent", ChangeEvent.HOME_GOAL);
		params.put("eventId", 1953893);
		Score score = new Score();
		params.put("homeScore", score );
		params.put("awayScore", score);
		
		helper.sendTopicMessage(params );
	}
	

}
