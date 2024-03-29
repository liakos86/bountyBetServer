package gr.server.data.api.websocket;

import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;

import gr.server.def.websocket.WebSocketMessageHandler;

@ClientEndpoint
public class SportScoreWebSocketClient {
	
	Session userSession = null;
    private WebSocketMessageHandler messageHandler;
    private URI webSocketUri;

    public SportScoreWebSocketClient(URI endpointURI, WebSocketMessageHandler handler) {
    	this.messageHandler = handler;
    	this.webSocketUri = endpointURI;
    	
        connect();
        
    }
    
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("--- Connected " + session.getId());
		this.userSession = session;
	}
	
	/**
     * Callback hook for Message Events. 
     * This method will be invoked when a client receives a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

   @OnMessage
   public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }

   /**
    * TODO: Socket might close with 1006.
    * 
    * Check time outs!
    * 
    * @param session
    * @param closeReason
    */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("--- Session: " + session.getId());
		System.out.println("--- Closing because: " + closeReason);
		this.userSession = null;
		
		connect();
		System.out.println("--- RECONNECTED!!!!!! " + closeReason);
		System.out.println("--- RECONNECTED!!!!!! " + closeReason);
		System.out.println("--- RECONNECTED!!!!!! " + closeReason);
		System.out.println("--- RECONNECTED!!!!!! " + closeReason);
	}

	public void sendMessage(String message) {
		if (userSession == null) {
			System.out.println("SOCKET CLOSED");	
		}
		
		this.userSession.getAsyncRemote().sendText(message);
	}
	
	private void connect() {
		try {
        	final ClientManager client = ClientManager.createClient();
        	System.getProperties().put(SSLContextConfigurator.TRUST_STORE_FILE, "cacerts.jks");
        	System.getProperties().put(SSLContextConfigurator.TRUST_STORE_PASSWORD, "");
        	final SSLContextConfigurator defaultConfig = new SSLContextConfigurator();
        	defaultConfig.retrieve(System.getProperties());

        	SSLEngineConfigurator sslEngineConfigurator =
        	    new SSLEngineConfigurator(defaultConfig, true, false, false);
        	client.getProperties().put(ClientProperties.SSL_ENGINE_CONFIGURATOR,
        	    sslEngineConfigurator);
        	client.connectToServer(this , webSocketUri);
        	
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
	
}
