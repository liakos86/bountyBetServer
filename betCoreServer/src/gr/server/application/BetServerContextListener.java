package gr.server.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import gr.server.bets.settle.impl.UserBetHandler;
import gr.server.bets.settle.impl.UserBetPredictionHandler;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.global.helper.mock.MockApiDataFetchHelper;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;
import gr.server.mongo.util.MongoUtils;
import gr.server.program.SettleProgram;
import gr.server.transaction.helper.MongoTransactionalBlock;

@WebListener
public class BetServerContextListener implements ServletContextListener {
	
	public static String ENV = System.getProperty("gr.server.environment");

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

//		Runnable fetchEventsTask = () -> { MockApiDataFetchHelper.fetchEventsIntoLeagues(); };
		Runnable fetchEventsTask = () -> { ApiDataFetchHelper.fetchEventsIntoLeaguesAndExtractMatchEvents(); };
		ScheduledExecutorService fetchEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 0, 30, TimeUnit.MINUTES);
		
		Runnable fetchLiveEventsTask = () -> { ApiDataFetchHelper.fetchLiveEventsIntoLeagues(); };
		ScheduledExecutorService fetchLiveEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchLiveEventsExecutor.scheduleAtFixedRate(fetchLiveEventsTask, 1, 20, TimeUnit.MINUTES);
		
		Runnable fetchLeagueTablesTask = () -> { ApiDataFetchHelper.fetchLeagueStandings(); };
		ScheduledExecutorService fetchLeagueTablesExecutor = Executors.newSingleThreadScheduledExecutor();
//		fetchLeagueTablesExecutor.schedule(fetchLeagueTablesTask, 20, TimeUnit.SECONDS);
		
		
//		Runnable fetchPlayerStatsTask = () -> { ApiDataFetchHelper.fetchPlayerStatistics(); };
//		ScheduledExecutorService fetchPlayerStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
//		fetchPlayerStatisticsExecutor.schedule(fetchPlayerStatsTask, 40, TimeUnit.SECONDS);
		
		
		//SportScoreWebSocketClient webSocketClient = initiateWebSocket();
		
		//Runnable keepWebSocketAliveTask = () -> { webSocketClient.sendMessage(SportScoreApiConstants.SOCKET_KEEP_ALIVE_MSG); };
		//ScheduledExecutorService maintainWebSocketExecutorTask = Executors.newSingleThreadScheduledExecutor();
		//maintainWebSocketExecutorTask.scheduleAtFixedRate(keepWebSocketAliveTask, 0, 10, TimeUnit.SECONDS);
		
//		MockApiDataFetchHelper mHelper = new MockApiDataFetchHelper();
//		Runnable fetchLiveStatisticsTask = () -> { mHelper.updateLiveStats(); };
//		ScheduledExecutorService updateStatsExecutor = Executors.newSingleThreadScheduledExecutor();
//		updateStatsExecutor.scheduleAtFixedRate(fetchLiveStatisticsTask, 1, 100, TimeUnit.SECONDS);
//		updateStatsExecutor.schedule(fetchLiveStatisticsTask, 1, TimeUnit.MINUTES);
		
		
//		Runnable mockRedCardsTask = () -> { mHelper.mockRedCards(); };
//		ScheduledExecutorService cExecutor = Executors.newSingleThreadScheduledExecutor();
//		cExecutor.scheduleAtFixedRate(mockRedCardsTask, 2, 1, TimeUnit.MINUTES);
		
		
//		Runnable mockRedCardsTask = () -> { RestApplication.sendMockFirebaseTopicMessage(); };
//		ScheduledExecutorService cExecutor = Executors.newSingleThreadScheduledExecutor();
//		cExecutor.scheduleAtFixedRate(mockRedCardsTask, 10, 20, TimeUnit.SECONDS);
		

//		Runnable settleBetsRunnable = () -> { try {
//			new MongoClientHelperImpl().settleOpenBets();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}};
//		ScheduledExecutorService settleBetsRunnableTask = Executors.newScheduledThreadPool(2);
//		settleBetsRunnableTask.scheduleAtFixedRate(settleBetsRunnable, 10, 5, TimeUnit.MINUTES);
		
//		Runnable settlePredsRunnable = () -> { try {
//			new MongoClientHelperImpl().settlePredictions(FootballApiCache.FINISHED_EVENTS);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}};
		
//		ScheduledExecutorService settlePredsRunnableTask = Executors.newScheduledThreadPool(UserBetPredictionHandler.NUM_WORKERS);
//		settlePredsRunnableTask.scheduleAtFixedRate(settlePredsRunnable, 10, 3, TimeUnit.MINUTES);
//		
		/** new preds **/
		
	
		ExecutorService subscribersPredictionSettling = Executors.newFixedThreadPool(UserBetPredictionHandler.NUM_WORKERS);
		
		Runnable settlePredsRunnableOrchestrator = () -> { 
			
			try {
					int batch = (FootballApiCache.FINISHED_EVENTS.size() / UserBetPredictionHandler.NUM_WORKERS) > 0 ?
							FootballApiCache.FINISHED_EVENTS.size() / UserBetPredictionHandler.NUM_WORKERS 
							: FootballApiCache.FINISHED_EVENTS.size() % UserBetPredictionHandler.NUM_WORKERS;
				
					for (int i = 0; i < UserBetPredictionHandler.NUM_WORKERS; i++) {
			            subscribersPredictionSettling.submit(new UserBetPredictionHandler(batch));
			        }
			
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settlePredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settlePredsRunnableOrchestratorTask.scheduleAtFixedRate(settlePredsRunnableOrchestrator, 3, 3, TimeUnit.MINUTES);
		
		
		
		// bets
		
		ExecutorService subscribersBetSettling = Executors.newFixedThreadPool(UserBetHandler.NUM_WORKERS);
		
		Runnable settleBetsRunnableOrchestrator = () -> { 
			
			try {
				
				 MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				 Bson pendingOrPendingLostBetsFilter = mongoClientHelperImpl.pendingOrPendingLostBetsFilter();
				 long allUnsettledBetsSize = mongoClientHelperImpl.fetchFilterSize(CollectionNames.BETS, pendingOrPendingLostBetsFilter);
				
				 System.out.println("UNSETTLED BETS ARE: " + allUnsettledBetsSize);
				 
				long batchSize = (allUnsettledBetsSize / UserBetHandler.NUM_WORKERS) > 0 ?
						allUnsettledBetsSize / UserBetHandler.NUM_WORKERS 
						: allUnsettledBetsSize % UserBetHandler.NUM_WORKERS;
				
				FindIterable<Document> iterable = MongoUtils.getMongoCollection(CollectionNames.BETS).find(pendingOrPendingLostBetsFilter).batchSize((int) batchSize);
			
				List<Document> userBetsDocument = new ArrayList<>();
				for (Document bet : iterable) {
					userBetsDocument.add(bet);
					
					if (userBetsDocument.size() == batchSize) {						
						subscribersBetSettling.submit(new UserBetHandler(new HashSet<>(userBetsDocument)));
						userBetsDocument.clear();
					}
					
		        }
				
				if (!userBetsDocument.isEmpty()) {
					subscribersBetSettling.submit(new UserBetHandler(new HashSet<>(userBetsDocument)));
				}
			
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settleBetsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settleBetsRunnableOrchestratorTask.scheduleAtFixedRate(settleBetsRunnableOrchestrator, 6, 3, TimeUnit.MINUTES);
		
		
		
		/** close month winner **/
		
		Runnable closeMonthBalancesRunnable = () -> { 
			
			try {
					new MongoClientHelperImpl().closeMonthlyBalancesAndComputeMonthWinner();
			
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService closeMonthBalancesRunnableTask = Executors.newScheduledThreadPool(1);
		closeMonthBalancesRunnableTask.scheduleAtFixedRate(closeMonthBalancesRunnable, 0, 3, TimeUnit.MINUTES);
		
	
		
	}
	
//	private void settleBets() {
//		Set<MatchEvent> todaysFinishedEvents = FootballApiCache.ALL_EVENTS.values().stream().filter(match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus())).collect(Collectors.toSet());
//		
//		new MongoTransactionalBlock() {
//			
//			@Override
//			public void begin() throws Exception {
//				int settled = new MongoClientHelperImpl().settlePredictions(session, todaysFinishedEvents);
//				//logger.log(Level.INFO, "Settled " + settled + " predictions");
//				System.out.println("Settled " + settled + " predictions");
//			}
//		}.execute();
//		
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//				System.out.println("Working in thread: " + Thread.currentThread().getName());
//				int settled = new MongoClientHelperImpl().settleOpenBets(session);
//				System.out.println("Settled " + settled + " bets");
////				logger.log(Level.INFO, "Settled " + settled + " bets");
//				
//			}
//		}.execute();
//	}

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
