package gr.server.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.global.helper.mock.MockApiDataFetchHelper;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;
import gr.server.util.TimerTaskHelper;

@WebListener
public class BetServerContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SHUTTING DOWN");
	}
	
	/**
	 * First we retrieve all the matches for today.
	 * After that we retrieve the current live matches once.
	 * Finally we open a persistent web socket connection with the api server.
	 * Any changes will be pushed back to the server.
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		MockApiDataFetchHelper.fetchSections();
		//ApiDataFetchHelper.fetchSections();
		//System.out.println(RestApplication.SECTIONS.size() + " SECTIONS **********");
		
		MockApiDataFetchHelper.fetchLeagues();
//		ApiDataFetchHelper.fetchLiveEventsIntoLeagues();
		ApiDataFetchHelper.fetchEventsIntoLeagues();
		MockApiDataFetchHelper.fetchSeasonsStandingsIntoLeagues();
		SportScoreWebSocketClient webSocketClient = initiateWebSocket();
		
		//TODO: Device should track time
		TimerTask liveMatchMinuteUpdateTimerTask = TimerTaskHelper.liveMatchMinuteUpdateTimerTask();
		Timer liveMatchMinuteUpdateTimer = new Timer("liveMatchMinuteUpdateTimer");
		liveMatchMinuteUpdateTimer.schedule(liveMatchMinuteUpdateTimerTask,  new Date(), 60000);
		
		
		TimerTask maintainWebSocketTimerTask = TimerTaskHelper.maintainWebSocketTask(webSocketClient);
		Timer maintainWebSocketTimer = new Timer("maintainWebSocketTimer");
		maintainWebSocketTimer.schedule(maintainWebSocketTimerTask,  new Date(), 15000);
		
		
		TimerTask activeMqTimerTask = TimerTaskHelper.sendTopicMessageTask();
		Timer activeMqTimerTaskTimer = new Timer("activeMqTimerTaskTimer");
		activeMqTimerTaskTimer.schedule(activeMqTimerTask,  new Date(), 20000);
		
		
	}


	private SportScoreWebSocketClient initiateWebSocket() {
		URI uri = null;
		try {
			uri = new URI("wss://tipsscore.com:2083/app/7UXH2sNFqpVAO6FebyTKpujgfy8BUnM?protocol=7&client=js&version=5.0.3&flash=false");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		SportScoreWebSocketClient client = new SportScoreWebSocketClient(uri, new WebSocketMessageHandlerImpl());
		client.sendMessage("{\"event\":\"pusher:subscribe\",\"data\":{\"channel\":\"en-football-list\"}}");
		return client;
	}

}
