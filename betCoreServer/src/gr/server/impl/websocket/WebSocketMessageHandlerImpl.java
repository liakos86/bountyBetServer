package gr.server.impl.websocket;

import javax.jms.JMSException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.api.model.events.Updates;
import gr.server.data.live.helper.LiveUpdatesHelper;
import gr.server.def.websocket.WebSocketMessageHandler;

public class WebSocketMessageHandlerImpl
implements WebSocketMessageHandler{
	
	LiveUpdatesHelper helper = new LiveUpdatesHelper();
	
	public void handleMessage(String msg) {
		if (msg.equals("{\"event\":\"pusher:pong\"}") || msg.startsWith("{\"event\":\"pusher_internal:subscription_succeeded\"")) {
			return;
		}
		
		
		System.out.println("RECEIVED " + msg);
		String replacedMsg = msg
				.replace("\\\"", "\"")
				.replace("\"{", "{")
				.replace("\"}", "}")
				.replace("\"main_odds\":[]", "\"main_odds\": null")
				.replace("\"home_score\":[]", "\"home_score\": null")
				.replace("\"away_score\":[]", "\"away_score\": null");
		Updates updates = new Gson().fromJson(replacedMsg, new TypeToken<Updates>() {}.getType());
		try {
			helper.updateLiveDetails(updates);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
