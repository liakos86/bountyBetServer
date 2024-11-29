package gr.server.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;

public class TestSocketConn {
	
	@Test
	public void testPlaceBet_success(){
		SportScoreWebSocketClient webSocketClient = initiateWebSocket();
		
		webSocketClient.sendMessage(SportScoreApiConstants.SOCKET_KEEP_ALIVE_MSG);
				
	}
	
	private SportScoreWebSocketClient initiateWebSocket() {
		URI uri = null;
		try {
			uri = new URI(SportScoreApiConstants.SOCKET_CONN_URL);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		SportScoreWebSocketClient client = new SportScoreWebSocketClient(uri, new WebSocketMessageHandlerImpl());
		client.sendMessage(SportScoreApiConstants.SOCKET_BOOTSTRAP_MSG);
		return client;
	}

}
