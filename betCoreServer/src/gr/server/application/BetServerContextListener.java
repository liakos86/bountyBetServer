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

import gr.server.util.TimerTaskHelper;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.global.helper.mock.MockApiDataFetchHelper;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;

@WebListener
public class BetServerContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SERVER SHUTTING DOWN");
		RestApplication.disconnectActiveMq();
	}
	
	/**
	 * First we connect to the local active mq in order to produce live topic messages later.
	 * Secondly we get all the available sections. (e.g. World, Africa, Greece etc.
	 * Then we get all the leagues that belong to the sections.
	 * Afterwards we retrieve all the REDIS cached matches for today +/- 3 days.
	 * Then, if we are missing any date from above, we retrieve all the matches from SPORTSCORE api.
	 * After that we retrieve the current live matches once.
	 * Finally we open a persistent web socket connection with the api server.
	 * Any live match events will be published to the live events ACTIVE MQ topic.
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		RestApplication.connectActiveMq();
		
		MockApiDataFetchHelper.fetchSections();//reducing calls
		MockApiDataFetchHelper.fetchLeagues();//reducing calls
//		
//		//RedisFetchHelper.fetchCachedEventsIntoLeagues();
//		
		ApiDataFetchHelper.fetchEventsIntoLeagues();
		
		SportScoreWebSocketClient webSocketClient = initiateWebSocket();

		TimerTask maintainWebSocketTimerTask = TimerTaskHelper.maintainWebSocketTask(webSocketClient);
		Timer maintainWebSocketTimer = new Timer("maintainWebSocketTimer");
		maintainWebSocketTimer.schedule(maintainWebSocketTimerTask,  new Date(), 10000);
		
//		Calendar cal = Calendar.getInstance();
//		cal.add(Calendar.MINUTE, 1);
//		Date inAMinute = cal.getTime();

//		TimerTask settleFinishedEventsTimerTask = TimerTaskHelper.settleFinishedEvents();
//		Timer settleFinishedEventsTimer = new Timer("settleFinishedEventsTimer");
//		settleFinishedEventsTimer.schedule(settleFinishedEventsTimerTask,  inAMinute, 180000);
		
//		TimerTask settleOpenBetsTimerTask = TimerTaskHelper.settleOpenBets();
//		Timer settleOpenBetsTimer = new Timer("settleOpenBetsTimer");
//		settleOpenBetsTimer.schedule(settleOpenBetsTimerTask, inAMinute, 200000);
		
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
