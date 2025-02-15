package gr.server.transaction.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import gr.server.bets.settle.impl.UserBetDelayedHandler;
import gr.server.bets.settle.impl.UserBetHandler;
import gr.server.bets.settle.impl.UserBetPredictionHandler;
import gr.server.bets.settle.impl.UserBetWithdrawnPredictionHandler;
import gr.server.common.MongoCollectionConstants;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.bean.OpenBetDelayedLeagueDatesBean;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Leagues;
import gr.server.data.api.model.league.Sections;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.user.model.objects.User;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.mongo.util.MongoUtils;

public class ExecutorsBetHelper {
	
	
	public void scheduleSections(){
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
	}
	
	public void scheduleLeagues() {
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
	}

	public void scheduleEvents() {

		//events
		ExecutorService fetchEventsService = Executors.newFixedThreadPool(1);


		Runnable fetchEventsTask = () -> {
			
			fetchEventsService.submit(() -> {
			ApiDataFetchHelper.fetchEventsIntoLeaguesAndExtractMatchEvents();
			});
			};
			
			
		ScheduledExecutorService fetchEventsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchEventsExecutor.scheduleAtFixedRate(fetchEventsTask, 20, 60*60, TimeUnit.SECONDS);
				
				
	}

	public void scheduleLiveEvents() {
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
	}

	public void scheduleSettlePredictions() {
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
					System.out.println("SETTLED PRED ERROR");
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settlePredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settlePredsRunnableOrchestratorTask.scheduleAtFixedRate(settlePredsRunnableOrchestrator, 3, 3, TimeUnit.MINUTES);
	}
	
	public void scheduleSettleDelayedPredictions() {
		/** new preds **/
		ExecutorService subscribersPredictionSettling = Executors.newFixedThreadPool(1);
		
		Runnable settlePredsRunnableOrchestrator = () -> 	
		{
			MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
			 Bson pendingOrPendingLostBetsFilter = mongoClientHelperImpl.pendingOrPendingLostBetsFilter();
			 long millisYesterday = System.currentTimeMillis() - ( 12 * 60 * 60 * 1000);
			// System.out.println("MILLL::" + millisYesterday);
			 Bson beforeYesterdayBetsFilter = Filters.lt(MongoFields.USER_BET_PLACEMENT_MILLIS, millisYesterday);
			 Bson combined = Filters.and(pendingOrPendingLostBetsFilter, beforeYesterdayBetsFilter);
			 long allUnsettledBetsSize = mongoClientHelperImpl.fetchFilterSize(MongoCollectionConstants.USER_BETS, combined);
			
			 System.out.println("************ALL UNSETTLED BETS BEFORE YESTERDAY:::" + allUnsettledBetsSize);
			if (allUnsettledBetsSize == 0) {
				return;
			}
			
			MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
			MongoCollection<Document> predsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
			FindIterable<Document> delayedDocs = betsCollection.find(combined);
			
			Map<Integer, Set<String>> beansMap = new HashMap<>();
			for (Document doc : delayedDocs) {
				String mongoBetId = doc.getObjectId(MongoFields.MONGO_ID).toString();
				System.out.println("OPEN BET:::" + mongoBetId);
				Bson betIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, mongoBetId);
				Bson predStatusFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_STATUS, PredictionStatus.PENDING.getCode());
				Bson filter = Filters.and(betIdFilter, predStatusFilter);
				FindIterable<Document> delayedPreds = predsCollection.find(filter);
				for (Document pr : delayedPreds) {
					Integer leagueId = pr.getInteger(MongoFields.USER_BET_PREDICTION_BET_LEAGUE_ID);
					System.out.println("OPEN PRED FOR LEAGUE:::"+ leagueId);
					if (leagueId == null) {
						System.out.println("LEAGUE MISSING:::");
						throw new RuntimeException("LEAGUE MISSING");
					}
					
					if (beansMap.get(leagueId) == null) {
						beansMap.put(leagueId, new HashSet<>());
					}
					
					Set<String> datesForLeague = beansMap.get(leagueId);
					
					
					String startAt = pr.getString(MongoFields.USER_BET_PREDICTION_BET_START_AT);
					
//					System.out.println("START IS::" +startAt);
					
					SimpleDateFormat matchDateFormat = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT);
					Date dateFromStr;
					try {
						dateFromStr = matchDateFormat.parse(startAt);
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.GET_EVENTS_DATE_FORMAT);
						String finalDateStr = simpleDateFormat.format(dateFromStr);
						datesForLeague.add(finalDateStr);
						System.out.println("NEW DATE " + finalDateStr + " FOR " + leagueId);
					} catch (ParseException e) {
						System.out.println("DATE PARSE ERROR");
						e.printStackTrace();
					}
					
					
					
				}
			}
			
			Set<OpenBetDelayedLeagueDatesBean> beans = new HashSet<>();
			
			Set<Entry<Integer,Set<String>>> entrySet = beansMap.entrySet();
			for (Entry<Integer, Set<String>> entry : entrySet) {
				OpenBetDelayedLeagueDatesBean bean = new OpenBetDelayedLeagueDatesBean();
				bean.setLeagueId(entry.getKey());
				bean.getMissingDates().addAll(entry.getValue());
				beans.add(bean);
			}
			
			subscribersPredictionSettling.submit(new UserBetDelayedHandler(beans));
			     
		};
			    			
	 
			    		 
		
		ScheduledExecutorService settlePredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settlePredsRunnableOrchestratorTask.scheduleAtFixedRate(settlePredsRunnableOrchestrator, 2, 120, TimeUnit.MINUTES);
	}

	public void scheduleSettleBets() {
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


	}

	public void scheduleSettleWithdrawnPredictions() {
		// TODO Auto-generated method stub
		
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
		
		
		
	}

	public void scheduleMonthWinnerCheck() {
		// TODO Auto-generated method stub
		
		
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

	public void scheduleFetchLeaderBoard(int initialDelay, int repeatInterval, TimeUnit timeunit) {
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
		fetchLeadersExecutor.scheduleAtFixedRate(fetchLeadersTask, initialDelay, repeatInterval, timeunit);
	}

	public void scheduleFetchStandings() {
		ExecutorService fetchStandingsService = Executors.newFixedThreadPool(1);//LeagueStatsHandler.NUM_WORKERS);

		Runnable fetchStandingsTask = () -> {
			try {
				
				Set<League> batchLeagues = new HashSet<>();
					
				fetchStandingsService.submit(() -> {
					new ApiDataFetchHelper().fetchLeagueStandings(batchLeagues);
				});
			
		}catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		};
			
			
		ScheduledExecutorService fetchStandingsExecutor = Executors.newSingleThreadScheduledExecutor();
		fetchStandingsExecutor.scheduleAtFixedRate(fetchStandingsTask, 60, 3*60, TimeUnit.SECONDS);
		
		
	}

	public void scheduleFetchStatistics() {
		// TODO Auto-generated method stub
		

		
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
//						System.out.println("****NEW LIVE STATS SUBMIT " + batchSize);
						
						fetchStatsService.submit(() -> {
							new ApiDataFetchHelper().fetchEventStatistics(eventIds);
							
						});
						
						eventsToHandle.clear();
					}
		        }
				
				if (!eventsToHandle.isEmpty()) {
//					System.out.println("****NEW LIVE STATS SUBMIT EXTRA " + batchSize);
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

}
