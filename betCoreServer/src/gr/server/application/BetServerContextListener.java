package gr.server.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.global.helper.mock.MockApiDataFetchHelper;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.MongoTransactionalBlock;

@WebListener
public class BetServerContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SERVER SHUTTING DOWN");
//		RestApplication.disconnectActiveMq();

		MongoUtils.getMongoClient().close();
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
		
//		RestApplication.connectActiveMq();
		
		RestApplication.connectFirebase();
		
		MockApiDataFetchHelper.fetchSections();//reducing calls
		MockApiDataFetchHelper.fetchLeagues();//reducing calls

		
		Runnable fetchEventsTask = () -> { ApiDataFetchHelper.fetchEventsIntoLeagues(); };
		ScheduledExecutorService fetchEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 0, 15, TimeUnit.MINUTES);
		
		Runnable fetchLeagueTablesTask = () -> { ApiDataFetchHelper.fetchLeagueStandings(); };
		ScheduledExecutorService fetchLeagueTablesExecutor = Executors.newSingleThreadScheduledExecutor();
//		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 0, 15, TimeUnit.MINUTES);
		fetchLeagueTablesExecutor.schedule(fetchLeagueTablesTask, 20, TimeUnit.SECONDS);
		
		
		Runnable fetchPlayerStatsTask = () -> { ApiDataFetchHelper.fetchPlayerStatistics(); };
		ScheduledExecutorService fetchPlayerStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
//		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 0, 15, TimeUnit.MINUTES);
		fetchPlayerStatisticsExecutor.schedule(fetchPlayerStatsTask, 40, TimeUnit.SECONDS);
		
		
		//SportScoreWebSocketClient webSocketClient = initiateWebSocket();
		
		//Runnable keepWebSocketAliveTask = () -> { webSocketClient.sendMessage(SportScoreApiConstants.SOCKET_KEEP_ALIVE_MSG); };
		//ScheduledExecutorService maintainWebSocketExecutorTask = Executors.newSingleThreadScheduledExecutor();
		//maintainWebSocketExecutorTask.scheduleAtFixedRate(keepWebSocketAliveTask, 0, 10, TimeUnit.SECONDS);
		
//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//		Runnable fetchLiveStatisticsTask = () -> { mHelper.updateLiveStats(); };
//		ScheduledExecutorService updateStatsExecutor = Executors.newSingleThreadScheduledExecutor();
////		updateStatsExecutor.scheduleAtFixedRate(fetchLiveStatisticsTask, 1, 60, TimeUnit.MINUTES);
//		updateStatsExecutor.schedule(fetchLiveStatisticsTask, 1, TimeUnit.MINUTES);
		
		
//		Runnable mockRedCardsTask = () -> { mHelper.mockRedCards(); };
//		ScheduledExecutorService cExecutor = Executors.newSingleThreadScheduledExecutor();
//		cExecutor.scheduleAtFixedRate(mockRedCardsTask, 2, 1, TimeUnit.MINUTES);
		
		
//		Runnable mockRedCardsTask = () -> { RestApplication.sendMockFirebaseTopicMessage(); };
//		ScheduledExecutorService cExecutor = Executors.newSingleThreadScheduledExecutor();
//		cExecutor.scheduleAtFixedRate(mockRedCardsTask, 10, 20, TimeUnit.SECONDS);
		

//		Runnable settleBetsRunnable = () -> { settleBets(); };
//		ScheduledExecutorService settleBetsRunnableTask = Executors.newScheduledThreadPool(2);
//		settleBetsRunnableTask.scheduleAtFixedRate(settleBetsRunnable, 0, 3, TimeUnit.MINUTES);
		
		
		// Response is a message ID string.
		
	}
	
	private void settleBets() {
		Set<MatchEvent> todaysFinishedEvents = FootballApiCache.ALL_EVENTS.values().stream().filter(match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus())).collect(Collectors.toSet());
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				int settled = new MongoClientHelperImpl().settlePredictions(session, todaysFinishedEvents);
				//logger.log(Level.INFO, "Settled " + settled + " predictions");
				System.out.println("Settled " + settled + " predictions");
			}
		}.execute();
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				System.out.println("Working in thread: " + Thread.currentThread().getName());
				int settled = new MongoClientHelperImpl().settleOpenBets(session);
				System.out.println("Settled " + settled + " bets");
//				logger.log(Level.INFO, "Settled " + settled + " bets");
				
			}
		}.execute();
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
