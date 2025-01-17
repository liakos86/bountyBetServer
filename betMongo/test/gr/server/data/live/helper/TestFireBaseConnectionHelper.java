package gr.server.data.live.helper;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.Score;
import gr.server.data.live.helper.FireBaseConnectionHelper;

public class TestFireBaseConnectionHelper {
	
	@Test
	public void test() throws Exception {
		FireBaseConnectionHelper helper = new FireBaseConnectionHelper();
		
		
		helper.connect();
		
		if (!helper.connected) {
			throw new Exception("Failed to connect");
		}
		
		
		Map<String, String> params = new HashMap<>();
		
		params.put("changeEvent","1");// ChangeEvent.HOME_GOAL.getChangeCode().toString());
		params.put("eventId", "2616094");
		Score score = new Score();
		params.put("homeScore", "4");
		params.put("awayScore", "2");
		
		helper.sendTopicMessage(params );
	}
	

}
