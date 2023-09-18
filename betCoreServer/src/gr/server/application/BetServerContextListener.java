package gr.server.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
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
		System.out.println("SERVER SHUTTING DOWN");
		RestApplication.disconnectActiveMq();
	}
	
	/**
	 * First we get all the available sections. (e.g. World, Africa, Greece etc.
	 * First we retrieve all the matches for today.
	 * After that we retrieve the current live matches once.
	 * Finally we open a persistent web socket connection with the api server.
	 * Any changes will be pushed back to the server.
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		RestApplication.connectActiveMq();
		
		MockApiDataFetchHelper.fetchSections();//reducing calls
		MockApiDataFetchHelper.fetchLeagues();//reducing calls
		
		ApiDataFetchHelper.fetchEventsIntoLeagues();
		
		SportScoreWebSocketClient webSocketClient = initiateWebSocket();

		TimerTask maintainWebSocketTimerTask = TimerTaskHelper.maintainWebSocketTask(webSocketClient);
		Timer maintainWebSocketTimer = new Timer("maintainWebSocketTimer");
		maintainWebSocketTimer.schedule(maintainWebSocketTimerTask,  new Date(), 10000);
		
		TimerTask settleFinishedEventsTimerTask = TimerTaskHelper.settleFinishedEvents();
		Timer settleFinishedEventsTimer = new Timer("settleFinishedEventsTimer");
		settleFinishedEventsTimer.schedule(settleFinishedEventsTimerTask,  new Date());
		
		TimerTask settleOpenBetsTimerTask = TimerTaskHelper.settleOpenBets();
		Timer settleOpenBetsTimer = new Timer("settleOpenBetsTimer");
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		Date inAMinute = cal.getTime();
		settleOpenBetsTimer.schedule(settleOpenBetsTimerTask, inAMinute, 20000);
		
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
