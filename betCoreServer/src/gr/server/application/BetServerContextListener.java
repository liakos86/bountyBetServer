package gr.server.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;

import gr.server.bets.settle.impl.UserBetHandler;
import gr.server.bets.settle.impl.UserBetPredictionHandler;
import gr.server.bets.settle.impl.UserBetWithdrawnPredictionHandler;
import gr.server.common.MongoCollectionConstants;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.handler.LeagueStatsHandler;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Sections;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.user.model.objects.User;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.client.SportScoreClient;
import gr.server.impl.websocket.WebSocketMessageHandlerImpl;
import gr.server.mongo.util.MongoUtils;
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

		
		RestApplication.connectFirebase();
		
		
		fetchSectionsAndLeaguesFromDbIntoCache();
		
		
		//sections
		ExecutorService fetchSectionsService = Executors.newFixedThreadPool(1);


		Runnable fetchSectionsTask = () -> {
			
			fetchSectionsService.submit(() -> {
				Sections sectionsFromFile = MockApiClient.getSectionsFromFile();// TODO change to api
				if (sectionsFromFile==null || sectionsFromFile.getData()==null || sectionsFromFile.getData().isEmpty()) {
					return;
				}
				
				new MongoClientHelperImpl().updateSections(sectionsFromFile.getData());
			});
			
		};
			
			
		ScheduledExecutorService fetchSectionsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchSectionsExecutor.scheduleAtFixedRate(fetchSectionsTask, 5, 48*60*60, TimeUnit.SECONDS);
		
		
		//leagues
		ExecutorService fetchLeaguesService = Executors.newFixedThreadPool(1);


		Runnable fetchLeaguesTask = () -> {
			
			fetchLeaguesService.submit(() -> {
				Leagues leaguesFromFile = MockApiClient.getLeaguesFromFile();// TODO change to api
				if (leaguesFromFile==null || leaguesFromFile.getData()==null || leaguesFromFile.getData().isEmpty()) {
					return;
				}
				
				new MongoClientHelperImpl().updateLeagues(leaguesFromFile.getData());
			});
			
		};
			
			
		ScheduledExecutorService fetchLeaguesExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchLeaguesExecutor.scheduleAtFixedRate(fetchLeaguesTask, 60, 48*60*60, TimeUnit.SECONDS);
		
		

		//events
		ExecutorService fetchEventsService = Executors.newFixedThreadPool(1);


		Runnable fetchEventsTask = () -> {
			
			fetchEventsService.submit(() -> {
			ApiDataFetchHelper.fetchEventsIntoLeaguesAndExtractMatchEvents();
			});
			};
			
			
		ScheduledExecutorService fetchEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 20, 60*60, TimeUnit.SECONDS);
		
		
		//live
		
		ExecutorService fetchLiveEventsService = Executors.newFixedThreadPool(1);

		
		Runnable fetchLiveEventsTask = () -> { 
			fetchLiveEventsService.submit(() ->{
				
				try {
			ApiDataFetchHelper.fetchLiveEventsIntoLeagues();
				}catch(Exception e) {
					e.printStackTrace();
				}
			});
			};
		
		
		ScheduledExecutorService fetchLiveEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchLiveEventsExecutor.scheduleAtFixedRate(fetchLiveEventsTask, 30, 15*60, TimeUnit.SECONDS);
		
		
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
		
		/** withdrawn preds **/
		
		
		ExecutorService subscribersWithDrawnPredictionSettling = Executors.newFixedThreadPool(UserBetWithdrawnPredictionHandler.NUM_WORKERS);
		
		Runnable settleWithdrawnPredsRunnableOrchestrator = () -> { 
			
			try {
				subscribersWithDrawnPredictionSettling.submit(new UserBetWithdrawnPredictionHandler(FootballApiCache.WITHDRAWN_EVENTS.size()));
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settleWithdrawnPredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settleWithdrawnPredsRunnableOrchestratorTask.scheduleAtFixedRate(settleWithdrawnPredsRunnableOrchestrator, 3, 3, TimeUnit.MINUTES);
		
		
		
		// bets
		
		ExecutorService subscribersBetSettling = Executors.newFixedThreadPool(UserBetHandler.NUM_WORKERS);
		
		Runnable settleBetsRunnableOrchestrator = () -> { 
			
			try {
				 MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				 Bson pendingOrPendingLostBetsFilter = mongoClientHelperImpl.pendingOrPendingLostBetsFilter();
				 long allUnsettledBetsSize = mongoClientHelperImpl.fetchFilterSize(MongoCollectionConstants.USER_BETS, pendingOrPendingLostBetsFilter);
				
				 System.out.println("************ALL UNSETTLED BETS " + allUnsettledBetsSize);
				if (allUnsettledBetsSize == 0) {
					return;
				}
				 
				 long batchSize = (allUnsettledBetsSize / UserBetHandler.NUM_WORKERS) > 0 ?
						allUnsettledBetsSize / UserBetHandler.NUM_WORKERS 
						: allUnsettledBetsSize % UserBetHandler.NUM_WORKERS;
				
				FindIterable<Document> iterable = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS).find(pendingOrPendingLostBetsFilter).batchSize((int) batchSize);
			
				List<Document> userBetsDocument = new ArrayList<>();
				for (Document bet : iterable) {
					userBetsDocument.add(bet);
					
					if (userBetsDocument.size() == batchSize) {
						System.out.println("****NEW BATCH SUBMIT " + batchSize);
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
		settleBetsRunnableOrchestratorTask.scheduleAtFixedRate(settleBetsRunnableOrchestrator, 3, 3, TimeUnit.MINUTES);


		
		
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
		
		
		//leader board
		ExecutorService fetchLeadersService = Executors.newFixedThreadPool(1);


		Runnable fetchLeadersTask = () -> {
			try {
			fetchLeadersService.submit(() -> {
				Map<Integer, List<User>> retrieveLeaderBoard = new MongoClientHelperImpl().retrieveLeaderBoard();
				FootballApiCache.LEADERS.clear();
				FootballApiCache.LEADERS.put(0, retrieveLeaderBoard.get(0));
				FootballApiCache.LEADERS.put(1, retrieveLeaderBoard.get(1));
			});
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		};
			
			
		ScheduledExecutorService fetchLeadersExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchLeadersExecutor.scheduleAtFixedRate(fetchLeadersTask, 60, 5*60, TimeUnit.SECONDS);
		
		
		//standings
		ExecutorService fetchStandingsService = Executors.newFixedThreadPool(1);//LeagueStatsHandler.NUM_WORKERS);


		Runnable fetchStandingsTask = () -> {
			try {
				
//				int size = FootballApiCache.ALL_LEAGUES.size();
//				int batchSize = size / 1;// LeagueStatsHandler.NUM_WORKERS;
				Set<League> batchLeagues = new HashSet<>();
					
				fetchStandingsService.submit(() -> {
					new ApiDataFetchHelper().fetchLeagueStandings(batchLeagues);
//					new LeagueStatsHandler(batchLeagues);
				});
			
		}catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		};
			
			
		ScheduledExecutorService fetchStandingsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchStandingsExecutor.scheduleAtFixedRate(fetchStandingsTask, 60, 3*60, TimeUnit.SECONDS);
		
		
		//live statistics
		ExecutorService fetchStatsService = Executors.newFixedThreadPool(4);//LeagueStatsHandler.NUM_WORKERS);

		Runnable fetchStatsTask = () -> {
			try {
				
				Set<Integer> eventIds = FootballApiCache.LIVE_EVENTS.keySet();
				int batchSize = eventIds.size() / 4;
				
				List<Integer> eventsToHandle = new ArrayList<>();
				for (Integer eventId : eventIds) {
					eventsToHandle.add(eventId);
					
					if (eventsToHandle.size() == batchSize) {
						System.out.println("****NEW LIVE STATS SUBMIT " + batchSize);
						
						fetchStatsService.submit(() -> {
							new ApiDataFetchHelper().fetchEventStatistics(eventIds);
							
						});
						
						eventsToHandle.clear();
					}
		        }
				
				if (!eventsToHandle.isEmpty()) {
					System.out.println("****NEW LIVE STATS SUBMIT EXTRA " + batchSize);
					fetchStatsService.submit(() -> {
						new ApiDataFetchHelper().fetchEventStatistics(eventIds);
					});
				}
			
		}catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		};
			
			
		ScheduledExecutorService fetchStatsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchStatsExecutor.scheduleAtFixedRate(fetchStatsTask, 120, 3*60, TimeUnit.SECONDS);
			
	}

	private void fetchSectionsAndLeaguesFromDbIntoCache() {
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				List<Section> sectionsFromDb = mongoClientHelperImpl.getSectionsFromDb(session);
				sectionsFromDb.forEach(s->FootballApiCache.ALL_SECTIONS.put(s.getId(), s));
				
				List<League> leaguesFromDb = mongoClientHelperImpl.getLeaguesFromDb(session);
				leaguesFromDb.forEach(l->FootballApiCache.ALL_LEAGUES.put(l.getId(), l));
				
				MongoUtils.DB_DATA_FETCHED = true;
				
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
