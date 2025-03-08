package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import gr.server.common.MongoCollectionConstants;
import gr.server.common.ServerConstants;
import gr.server.common.enums.PurchasedProduct;
import gr.server.common.logging.CommonLogger;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.bean.OpenBetDelayedLeagueDatesBean;
import gr.server.data.api.model.dto.LoginResponseDto;
import gr.server.data.api.model.events.Events;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionSettleStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.MongoFields;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.enums.UserLevel;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserAward;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserMonthlyBalance;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.data.user.model.objects.UserPurchase;
import gr.server.def.client.MongoClientHelper;
import gr.server.mongo.bean.PlaceBetResponseBean;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class MongoClientHelperImpl 
implements MongoClientHelper {
	
	static Logger logger = CommonLogger.logger;

	
	@Override
	public User createUser(User user) {
		
		MongoTransactionalBlock<Void> userBlock = new MongoTransactionalBlock<Void>() {
			@Override
			public void begin() throws Exception {
				System.out.println("Create user Working in thread: " + Thread.currentThread().getName());
				MongoCollection<Document> users = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);

				Document existingUserName = new Document(MongoFields.USERNAME, user.getUsername());
				Document existingEmail = new Document(MongoFields.EMAIL, user.getEmail());
				List<Document> filters = new ArrayList<>();
				filters.add(existingEmail);
				filters.add(existingUserName);
				Document orDocument = MongoUtils.getAndOrDocument(filters, MongoFields.OR);

				FindIterable<Document> find = users.find(session, orDocument);
				Document existingUser = find.first();
				if (existingUser != null) {

					boolean isValid = existingUser.getBoolean(MongoFields.VALIDATED);
					if (isValid) {
						user.setErrorMessage("Username or email already used");
					} else {
						user.setErrorMessage(user.getEmail() + " needs to validate email");
					}

					return;
				}

				Document newUserDocument = MongoUtils.getNewUserDocument(user);
				users.insertOne(session, newUserDocument);

				user.setMongoId(newUserDocument.getObjectId(MongoFields.MONGO_ID).toString());
				user.init();
				user.setUsername(newUserDocument.getString(MongoFields.USERNAME));
				user.setEmail(newUserDocument.getString(MongoFields.EMAIL));
				
				
				List<Document> monthDocuments = new ArrayList<>();
				for (int i = 1; i < 13; i++) {
					Document monthDocument = MongoUtils.getMonthlyBalanceDocument(user.getMongoId(), i);
					monthDocuments.add(monthDocument);
				}
				MongoCollection<Document> userBalanceCol = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
				userBalanceCol.insertMany(session, monthDocuments);
			
			}
		};
		
		userBlock.setRetries(3);
		
		boolean userCreated = userBlock.execute();
		
		if (userCreated && logger.isInfoEnabled()) {
			logger.info("Created new user:" + user);
		}
		
		return user;
		
	}

	@Override
	public LoginResponseDto loginUser(String username, String email, String password) {
		MongoClient client = MongoUtils.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(MongoCollectionConstants.BOUNTY_BET_DB)
				.getCollection(MongoCollectionConstants.USERS);

		Document existingUserName = new Document(MongoFields.USERNAME, username);
		Document existingEmail = new Document(MongoFields.EMAIL, email);
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = MongoUtils.getAndOrDocument(filters, MongoFields.OR);
		
		
		MongoTransactionalBlock<LoginResponseDto> mongoBlock = new MongoTransactionalBlock<LoginResponseDto>() {
			@Override
			public void begin() throws Exception {
				FindIterable<Document> find = users.find(orDocument);
				Document existingUser = find.first();
				
				result = new LoginResponseDto();
				if (existingUser != null) {
					boolean isValid = existingUser.getBoolean(MongoFields.VALIDATED);
					if (!isValid) {
						result.setErrorMessage(email + " needs to validate email");
					} else if (!password.trim().equals(existingUser.getString(MongoFields.PASSWORD))) {
						result.setErrorMessage("Wrong username/password combination");
					} 
					else {
						result.setMongoId(existingUser.getObjectId(MongoFields.MONGO_ID).toString());
					}
	
				} else {
					result.setErrorMessage("Wrong username/password combination");
				}
			}
		};
		
		mongoBlock.execute();

		
		return mongoBlock.getResult();
	}

	@Override
	public User getUser(String mongoId, int maxBetsToFetch, long millisToSearchBets, boolean includeUnsettled,
			boolean includeAwards, boolean includeBalances, boolean includeBounties) {
		
		MongoTransactionalBlock<User> mongoBlock = new MongoTransactionalBlock<User>() {
			
			@Override
			public void begin() throws Exception {
				MongoCollection<Document> users = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
				Bson userMongoIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoId));
				FindIterable<Document> usersFromMongo = users.find(session, userMongoIdFilter);

				Document userFromMongo = null;
				try {
					userFromMongo = usersFromMongo.first();
				}catch(Exception e) {
					CommonLogger.logger.error("COULD NOT RETRIEVE USER " + e.getClass().getCanonicalName());
					return;
				}
				
				if (userFromMongo == null) {
					return;
				}

				result = userFromMongoDocument(userFromMongo);
				
				if (maxBetsToFetch > 0) {
					result.setUserBets(getBetsForUser(session, mongoId, maxBetsToFetch, millisToSearchBets, includeUnsettled));
				}
				
				if (includeAwards) {
					result.setUserAwards(getAwardsForUser(session, mongoId));
				}
				
				if (includeBalances) {
					result.setBalances(getBalancesForUser(session, mongoId));
					UserMonthlyBalance currentBalanceObject = result.currentMonthBalance();
					result.setBalance(currentBalanceObject.getBalance());
					
					result.setMonthlyBetAmount(currentBalanceObject.getMonthlyBetAmount() != null ? currentBalanceObject.getMonthlyBetAmount() : 0);
					result.setMonthlyLostEventsCount(currentBalanceObject.getMonthlyLostEventsCount());
					result.setMonthlyWonEventsCount(currentBalanceObject.getMonthlyWonEventsCount());
					result.setMonthlyLostSlipsCount(currentBalanceObject.getMonthlyLostSlipsCount());
					result.setMonthlyWonSlipsCount(currentBalanceObject.getMonthlyWonSlipsCount());
					result.setPosition(userPosition(session, result));
					//System.out.println("Position is " + result.getPosition() + " for " + result.getUsername());
				}
				
				if(includeBounties) {
					result.setBounties(new ArrayList<>());
				}
				
				if (includeAwards && includeBounties) {
					result.setLevel(getUserLevel(result).getCode());
				}
				
				result.setMonthlyPurchases(getUserMonthlyPurchases(result));
			}
		};
		
		mongoBlock.execute();
		
		return mongoBlock.getResult();
	}
	
	@Override
	public PlaceBetResponseBean placeBet(UserBet userBet) {
		PlaceBetResponseBean responseBean = new PlaceBetResponseBean();
		
		userBet.setBetStatus(BetStatus.PENDING.getCode());
		userBet.setBetPlacementMillis(System.currentTimeMillis());
		
		int currentMonth = LocalDate.now().getMonthValue();
		userBet.setBelongingMonth(currentMonth);
		userBet.setBelongingYear(LocalDate.now().getYear());
		
		User user = new MongoClientHelperImpl().getUser(userBet.getMongoUserId(), 0, 0, false, false, true, false);

		if(userBet.getBetAmount()<=0 || insufficientFundsFor(user, userBet)) {
			responseBean.setBetPlacementStatus(BetPlacementStatus.FAILED_INSUFFICIENT_FUNDS);
			return responseBean;
		}
		
		if (liveGameInPredictions(userBet.getPredictions())) {
			responseBean.setBetPlacementStatus( BetPlacementStatus.FAILED_MATCH_IN_PROGRESS);
			return responseBean;
		}
		
		if (predictionsSpanNextMonth(userBet.getPredictions())) {
			logger.info("Matches exist into next month");
			responseBean.setBetPlacementStatus( BetPlacementStatus.FAIL_GENERIC);
			return responseBean;
		}
		
		boolean inserted = new MongoTransactionalBlock<Void>() {
			@Override
			public void begin() throws Exception {
				
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
				Document newBet = MongoUtils.getBetDocument(userBet);
				betsCollection.insertOne(session, newBet);
				
				String newBetMongoId = newBet.getObjectId(MongoFields.MONGO_ID).toString();
				
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
				
				Runnable storeTeamsTask = () -> { userBet.getPredictions().forEach(p -> storeTeams(p.getEventId())); };
				ScheduledExecutorService storeTeamsExecutor = Executors.newSingleThreadScheduledExecutor();
				storeTeamsExecutor.schedule(storeTeamsTask, 5, TimeUnit.SECONDS);
				
								
				List<Document> newPredictions = MongoUtils.getPredictionsDocuments(userBet, newBetMongoId);
				predictionsCollection.insertMany(session, newPredictions);
				
				userBet.setMongoId(newBetMongoId);
				responseBean.setBetId(newBetMongoId);
				
				MongoCollection<Document> userBalanceCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
				Bson filterUser = Filters.eq(MongoFields.MONGO_USER_ID, userBet.getMongoUserId());
				Bson filterMonth = Filters.eq(MongoFields.USER_BALANCE_MONTH, currentMonth);
				
				Bson increaseBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, (-1 * userBet.getBetAmount()));
				Bson increaseBetAmountDocument = Updates.inc(MongoFields.USER_BALANCE_BET_AMOUNT_MONTHLY, (1 * userBet.getBetAmount()));
				Bson userCombinedUpdates = Updates.combine(increaseBalanceDocument, increaseBetAmountDocument);
				
				
				userBalanceCollection.findOneAndUpdate(session, Filters.and(filterUser, filterMonth), userCombinedUpdates);
				
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
				Bson filterUserMongoId = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoUserId()));

				Bson increaseBetAmountOverallDocument = Updates.inc(MongoFields.USER_BET_AMOUNT_OVERALL, (1.0d * userBet.getBetAmount()));
				usersCollection.findOneAndUpdate(session, filterUserMongoId, increaseBetAmountOverallDocument);
				
				double remainingCredits = user.getRemainingCredits();
				if (remainingCredits > 0) {
					Bson increaseUserCreditsDocument = null;
					if (userBet.getBetAmount() >= remainingCredits) {
						increaseUserCreditsDocument = Updates.set(MongoFields.USER_PURCHASE_CREDITS, 0d);
					}else {
						increaseUserCreditsDocument = Updates.inc(MongoFields.USER_PURCHASE_CREDITS, (-1 * userBet.getBetAmount()));
					}
					
					userBalanceCollection.updateMany(session, filterUser, increaseUserCreditsDocument);
				}
				
			}

			//TODO: this should be done in the background,dont care about failure
			private void storeTeams(int eventId) {
				MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(eventId);
				Map<Integer, Team> teamsToInsert = new HashMap<>();
				teamsToInsert.put(matchEvent.getHome_team().getId(), matchEvent.getHome_team());
				teamsToInsert.put(matchEvent.getAway_team().getId(), matchEvent.getAway_team());
				MongoCollection<Document> teamsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.TEAM);
				Bson homeTeamFilter = Filters.eq(MongoFields.ID, matchEvent.getHome_team().getId());
				Bson awayTeamFilter = Filters.eq(MongoFields.ID, matchEvent.getAway_team().getId());
				Bson orFilter = Filters.or(homeTeamFilter, awayTeamFilter);
				FindIterable<Document> teamsDocuments = teamsCollection.find(orFilter);
				
				for (Document document : teamsDocuments) {
					Integer teamId = document.getInteger(MongoFields.ID);
					if (teamsToInsert.containsKey(teamId)) {
						teamsToInsert.remove(teamId);
					}
				}
				
				List<Document> teamsInsertDocuments = new ArrayList<>();
				for (Team team : teamsToInsert.values()) {
					teamsInsertDocuments.add( MongoUtils.getTeamDocument(team) );
				}
				
				if (!teamsToInsert.isEmpty()) {
					teamsCollection.insertMany(session, teamsInsertDocuments);
				}
				
			}
		}.execute();
		
		
		if (inserted) {
			responseBean.setBetPlacementStatus( BetPlacementStatus.PLACED);
			return responseBean;
		}
		
		responseBean.setBetPlacementStatus( BetPlacementStatus.FAIL_GENERIC);
		return responseBean;
	}

	private boolean predictionsSpanNextMonth(List<UserPrediction> predictions) {
		
		List<String> startTimes = new ArrayList<>();
		for (UserPrediction userPrediction : predictions) {
			MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(userPrediction.getEventId());
			startTimes.add(matchEvent.getStart_at());
		}
		
		if (DateUtils.gamesExistInNextMonth(startTimes)) {
//			System.out.println("**********************MATCH IN NEXT MONTH");
			return true;
		}
		
		return false;
	}

	private boolean insufficientFundsFor(User user, UserBet userBet) {
		double monthBalance = user.monthBalanceOf(userBet.getBelongingMonth()).getBalance();
		return monthBalance - userBet.getBetAmount() < 0;
	}

	@Override
	public Map<Integer, List<User>> retrieveLeaderBoard() {
		boolean updatedLeaderBoardBalances = updateMonthlyLeaderBoardBalances(DateUtils.getMonthAsInt(0), true);
		if (!updatedLeaderBoardBalances) {
			System.out.println("COULD NOT UPDATE LEADERBOARD BALANCES");
			logger.error("COULD NOT UPDATE LEADERBOARD BALANCES");
		}
		
		System.out.println("UPDATED LEADERBOARD");
		
		Map<Integer, List<User>> allLeaders = new HashMap<>(2);
		
		List<User> currentLeaders = new ArrayList<>();
		
		Document sortField = new Document(MongoFields.USER_BALANCE_LEADERBOARD, -1);
		
		Document findField = new Document(MongoFields.USER_BALANCE_MONTH, DateUtils.getMonthAsInt(0));
				
		MongoCollection<Document> balancesCol = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
		FindIterable<Document> find = balancesCol.find(findField).limit(20).sort(sortField);
		
		long tenDaysBefore = 10 * 1000 * 24 * 60 * 60;
		for(Document doc : find)
		{ 
			User user = getUser(doc.getString(MongoFields.MONGO_USER_ID), 5, tenDaysBefore, false, false, true, false);
			if (user==null) {
				continue;
			}
			
			user.setBalanceLeaderBoard(user.getBalance() - user.getRemainingCredits());
			
			currentLeaders.add(user);
		}
			
		allLeaders.put(0, currentLeaders);
		
		
		List<User> awardWinners = new ArrayList<>();
		MongoCollection<Document> awardsCol = MongoUtils.getMongoCollection(MongoCollectionConstants.AWARDS);
		Bson sortFieldAward = new Document(MongoFields.AWARD_PLACEMENT, -1);
		FindIterable<Document> awardsDocs = awardsCol.find().limit(12).sort(sortFieldAward);
		
		for(Document doc : awardsDocs)
		{ 
			User user = getUser(doc.getString(MongoFields.MONGO_USER_ID), 0, 0, false, true, false, false);
			if (user==null) {
				continue;
			}
			
			Integer awardMonth = doc.getInteger(MongoFields.AWARD_MONTH);
			Integer awardYear  = doc.getInteger(MongoFields.AWARD_YEAR);
			for (UserAward award : new ArrayList<>(user.getUserAwards())) {
				if (!awardMonth.equals(award.getAwardMonth()) || !awardYear.equals(awardYear)) {
					user.getUserAwards().remove(award);
				}
			}
			
			if(user.getUserAwards().size()!=1) {
				throw new RuntimeException("awards calc error");
			}
			
			awardWinners.add(user);
		}
			
		allLeaders.put(1, awardWinners);
		
		return allLeaders;
	}
	
	@Override
	public boolean closeMonthlyBalancesAndComputeMonthWinner() {
		boolean updatedLeaderBoardBalances = updateMonthlyLeaderBoardBalances(DateUtils.getMonthAsInt(-1), false);
		if (!updatedLeaderBoardBalances) {
			return false;
		}
		
		MongoTransactionalBlock<Boolean> monthlyComputation = new MongoTransactionalBlock<Boolean>() {
			
			@Override
			public void begin() throws Exception {
				result = false;
				boolean commonChecksToCloseMonth = commonChecksToCloseMonth(session);
				if (!commonChecksToCloseMonth) {
					return;
				}
				
				boolean closeMonthlyBalances = closeMonthlyBalances(session);
				if (!closeMonthlyBalances) {
					return;
				}
				
				computeMonthWinner(session);
				result = true;
			}
		};
		
		monthlyComputation.execute();
		return monthlyComputation.getResult();
		
	}
	
	boolean updateMonthlyLeaderBoardBalances(int month, boolean updateNextMonthAlso) {
		MongoTransactionalBlock<Boolean> monthlyComputation = new MongoTransactionalBlock<Boolean>() {
			
			@Override
			public void begin() throws Exception {
				result = false;
				
				int nextMonth = DateUtils.getNextMonthOf(month);
				
				MongoCollection<Document> collection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
//				Document fil = new Document(MongoFields.USER_PURCHASE_CREDITS, 0);	
//				Bson inc   = Updates.set(MongoFields.USER_PURCHASE_CREDITS, 0.0d);
//
//				collection.updateMany(fil, inc);
//				if (2>1) {
//					return;
//				}
				
				
				Document filterCurrentMonth = new Document(MongoFields.USER_BALANCE_MONTH, month);				
				Document filterNextMonth = new Document(MongoFields.USER_BALANCE_MONTH, nextMonth);
				Bson filterMonths = Filters.or(filterCurrentMonth, filterNextMonth);
				FindIterable<Document> documents = collection.find(filterMonths)
						.projection(new Document(MongoFields.USER_BALANCE, 1)
								.append(MongoFields.USER_PURCHASE_CREDITS, 1)
								.append(MongoFields.USER_BALANCE_MONTH, 1));
				
	            // Prepare bulk updates
	            List<com.mongodb.client.model.WriteModel<Document>> updates = new ArrayList<>();
	            for (Document doc : documents) {
//	            	System.out.println("DOC " + doc.get(MongoFields.MONGO_ID));
	            	int docMonth = doc.getInteger(MongoFields.USER_BALANCE_MONTH);
//	            	System.out.println("DOC::: " + doc.get(MongoFields.MONGO_ID));
	            	
	  
	            	Double userPurchaseCreditsRemaining = doc.getDouble(MongoFields.USER_PURCHASE_CREDITS);

	            	if (docMonth == month) {
	            	
	            	
		                Double currentMonthBalance = doc.getDouble(MongoFields.USER_BALANCE);
		                
		                if (currentMonthBalance != null) {
		                	if (userPurchaseCreditsRemaining == null) {
		                		userPurchaseCreditsRemaining = 0d;
		                	}
		                	
		                    Double currentMonthBalanceLeaderBoard = currentMonthBalance - userPurchaseCreditsRemaining;  // Perform subtraction
		                    updates.add(new com.mongodb.client.model.UpdateOneModel<>(
		                            new Document(MongoFields.MONGO_ID, doc.get(MongoFields.MONGO_ID)),  // Match by _id
		                            Updates.set(MongoFields.USER_BALANCE_LEADERBOARD, currentMonthBalanceLeaderBoard)  // Update C field
		                    ));
		                }
		                
	            	}else if (updateNextMonthAlso && docMonth == nextMonth) {
	            		Double startingBalance = ServerConstants.STARTING_BALANCE;
	            		if (userPurchaseCreditsRemaining == null) {
	                		userPurchaseCreditsRemaining = 0d;
	                	}
	                	
	            		Double nextMonthBalanceLeaderBoard = startingBalance + userPurchaseCreditsRemaining;  // Perform subtraction
	            		Bson updateLeaderBoardBalance = Updates.set(MongoFields.USER_BALANCE_LEADERBOARD, startingBalance);
	            		Bson updateBalance = Updates.set(MongoFields.USER_BALANCE, nextMonthBalanceLeaderBoard);
	            		Bson combinedUpdate = Updates.combine(updateBalance, updateLeaderBoardBalance);
	            		
	                    updates.add(new com.mongodb.client.model.UpdateOneModel<>(
	                            new Document(MongoFields.MONGO_ID, doc.get(MongoFields.MONGO_ID)),  // Match by _id
	                            combinedUpdate 
	                    ));
	            		
	            	}
	            }

	            if (!updates.isEmpty()) {
	                collection.bulkWrite(updates);
	            }
				
				result = true;
			}
		};
		
		monthlyComputation.execute();
		return monthlyComputation.getResult();
	}

	/**
	 * We receive the finished events only. 
	 * We fetch the settled events for today and skip them.
	 * Then create filters for predictions related to this event, but have incorrect prediction type. 
	 * Then for those related to the event and have correct prediction type. 
	 * For now the prediction category is only {@link PredictionCategory#FINAL_RESULT}.
	 */
	@Override
	public boolean settlePredictions(Set<MatchEvent> toSettle) throws Exception {
        List<UpdateManyModel<Document>> bulkOperations = new ArrayList<>();

		for (MatchEvent event : toSettle) {

			System.out.println("SETTLING " + event.getHome_team().getName());
			
			int winningPrediction = event.getWinner_code();
			if ( winningPrediction == 0) {
				System.out.println(event.getHome_team().getName() + " is finished but has no winner");
				logger.error("No winner found for " + event );
				throw new Exception("No winner found for " + event);
			}
			
			Bson eventIdFilter = Filters.eq(MongoFields.EVENT_ID, event.getId());
			Bson correctPredictionCatFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_CATEGORY,
					PredictionCategory.FINAL_RESULT.getCategoryCode());

			Bson incorrectPredictionTypeFilter = Filters.ne(MongoFields.USER_BET_PREDICTION_TYPE, winningPrediction);
			List<Bson> inCorrectPredictionFilters = new ArrayList<>();
			inCorrectPredictionFilters.add(eventIdFilter);
			inCorrectPredictionFilters.add(incorrectPredictionTypeFilter);
			inCorrectPredictionFilters.add(correctPredictionCatFilter);
			
			Bson unSuccessfulStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_STATUS,
					PredictionStatus.MISSED.getCode());
			
			Bson correctPredictionTypeFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_TYPE, winningPrediction);
			List<Bson> correctPredictionFilters = new ArrayList<>();
			correctPredictionFilters.add(eventIdFilter);
			correctPredictionFilters.add(correctPredictionTypeFilter);
			correctPredictionFilters.add(correctPredictionCatFilter);
			
			Bson successfulStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_STATUS,
					PredictionStatus.CORRECT.getCode());
			
			
			 UpdateManyModel<Document> updateUnsuccessfulDocuments = new UpdateManyModel<>(
					 Filters.and(inCorrectPredictionFilters),
					 unSuccessfulStatusDocument
			        );
			 
			 UpdateManyModel<Document> updateSuccessfulDocuments = new UpdateManyModel<>(
					 Filters.and(correctPredictionFilters),
					 successfulStatusDocument
			        );
			
			//collection.updateMany(session, Filters.and(inCorrectPredictionFilters), unSuccessfulStatusDocument);
//			collection.updateMany(session, Filters.and(correctPredictionFilters), successfulStatusDocument);

		
			bulkOperations.add(updateSuccessfulDocuments); 
			bulkOperations.add(updateUnsuccessfulDocuments); 
			 
//			storeSettledEvent(event.getId());
//			
//			++settled;
		
		}
		
		boolean updated = new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {	
				MongoCollection<Document> collection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
				BulkWriteResult bulkWrite = collection.bulkWrite(session, bulkOperations);
				logger.info(Thread.currentThread().getName() + ": " + bulkWrite);
				System.out.println(Thread.currentThread().getName() + ": " + bulkWrite);
			}
		}.execute();
		
		return updated;
	}
	
	/**
	 * We receive the finished events only. 
	 * We fetch the settled events for today and skip them.
	 * Then create filters for predictions related to this event, but have incorrect prediction type. 
	 * Then for those related to the event and have correct prediction type. 
	 * For now the prediction category is only {@link PredictionCategory#FINAL_RESULT}.
	 */
	@Override
	public boolean settleWithdrawnPredictions(Set<MatchEvent> toHandle) throws Exception {


		System.out.println("WITHDRAWN FOUND::::" + toHandle.size());
		
        List<UpdateManyModel<Document>> bulkOperations = new ArrayList<>();

		for (MatchEvent event : toHandle) {

			
			if( (!MatchEventStatus.POSTPONED.getStatusStr().equals(event.getStatus())
					&& !MatchEventStatus.CANCELLED.getStatusStr().equals(event.getStatus())
					&& !MatchEventStatus.CANCELED.getStatusStr().equals(event.getStatus())
					&& !MatchEventStatus.SUSPENDED.getStatusStr().equals(event.getStatus()))
					) {
				
				System.out.println(event + " IS NOT WITHDRAWN");
				logger.error(event + " IS NOT WITHDRAWN");
				throw new Exception(event + " IS NOT WITHDRAWN");
			}
			
			Bson eventIdFilter = Filters.eq(MongoFields.EVENT_ID, event.getId());
			
			Bson oddsFixDocument = Updates.set(MongoFields.USER_BET_PREDICTION_ODD_VALUE,
					1.0d);
			
			Bson updateStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_STATUS, 
					PredictionStatus.WITHDRAWN.getCode());
			
			Bson updateSettleStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, 
					PredictionSettleStatus.SETTLED.getCode());
			
			Bson updates = Updates.combine(oddsFixDocument, updateStatusDocument, updateSettleStatusDocument);
			
			 UpdateManyModel<Document> updateWithdrawnPredDocuments = new UpdateManyModel<>(
					 eventIdFilter,
					 updates
			        );
			 
		
			bulkOperations.add(updateWithdrawnPredDocuments); 
			 
		}
		
		boolean updated = new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {	
				MongoCollection<Document> collection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
				BulkWriteResult bulkWrite = collection.bulkWrite(session, bulkOperations);
				logger.info(Thread.currentThread().getName() + ": " + bulkWrite);
				System.out.println(Thread.currentThread().getName() + ": " + bulkWrite);
			}
		}.execute();
						
		return updated;
	}
	
	
	@Override
	public boolean storePurchase(UserPurchase verificationBean) {
		
		MongoTransactionalBlock<Boolean> mongoBlock = new MongoTransactionalBlock<Boolean>() {
			@Override
			public void begin() throws Exception {
				
				MongoCollection<Document> purchasesCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_PURCHASES);
				Bson filterUser = Filters.eq(MongoFields.MONGO_USER_ID, verificationBean.getMongoUserId());
				Bson filterToken = Filters.eq(MongoFields.TOKEN, verificationBean.getPurchaseToken());
				Bson filterUserWithToken = Filters.and(filterUser, filterToken);
				
				FindIterable<Document> userPurchases = purchasesCollection.find(filterUserWithToken);
				for (Document document : userPurchases) {
					if (verificationBean.getPurchaseToken().equals(document.getString(MongoFields.TOKEN))) {
						logger.error("TOKEN ALREADY PURCHASED::: " + verificationBean.getPurchaseToken());
						logger.info("TOKEN ALREADY PURCHASED::: " + verificationBean.getPurchaseToken());
						result = true;
						return;
					}
				}

				Document userPurchaseDocument = MongoUtils.getUserPurchaseDocument(verificationBean);
				purchasesCollection.insertOne(session, userPurchaseDocument);
				
				PurchasedProduct purchasedProduct = PurchasedProduct.fromName(verificationBean.getProductId());
				
				int currentMonth = LocalDate.now().getMonthValue();
				MongoCollection<Document> userBalanceCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
				Bson filterMonth = Filters.eq(MongoFields.USER_BALANCE_MONTH, currentMonth);
				Bson increaseBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, purchasedProduct.getCredits());
				userBalanceCollection.findOneAndUpdate(session, Filters.and(filterUser, filterMonth), increaseBalanceDocument);

				
				Bson increaseUserCreditsDocument = Updates.inc(MongoFields.USER_PURCHASE_CREDITS, purchasedProduct.getCredits());
				userBalanceCollection.updateMany(session, filterUser, increaseUserCreditsDocument);

				result = true;
			}
		};
		
		mongoBlock.execute();

		
		return mongoBlock.getResult();
		
	}
	

	
	/**
	 * We find all the bets which have unsettled predictions.
	 * For every bet we find its predictions.
	 * If a lost prediction is found, the bet is lost.
	 * If no lost predictions found, but at least one pending found, the bet is pending.
	 * If no lost and no pending predictions found, the bet is won.
	 * If the bet is won we assign the new balance to the user.
	 * Finally we increase the monthly and overall won and lost predictions and bets.
	 * 
	 * @param session
	 * @throws Exception
	 */
	@Override
    public boolean settleOpenBets(Set<Document> pendingBets) throws Exception {
		
		System.out.println("IN THREAD BET SETTLING SIZE:::" + pendingBets.size());
			
		Map<String, List<WriteModel<Document>>> updatesPerCollection = new HashMap<>();
		updatesPerCollection.put(MongoCollectionConstants.USER_BET_PREDICTIONS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USER_BETS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USERS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USER_MONTHLY_BALANCE, new ArrayList<>());
		
		
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
		MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
		MongoCollection<Document> balancesCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
		for (Document betDocument : pendingBets) {
			Map<String, List<WriteModel<Document>>> settleOpenBet = settleOpenBet(betDocument,  predictionsCollection);
			
			updatesPerCollection.get(MongoCollectionConstants.USER_BET_PREDICTIONS).addAll( settleOpenBet.get(MongoCollectionConstants.USER_BET_PREDICTIONS));
			updatesPerCollection.get(MongoCollectionConstants.USER_BETS).addAll( settleOpenBet.get(MongoCollectionConstants.USER_BETS));
			updatesPerCollection.get(MongoCollectionConstants.USERS).addAll( settleOpenBet.get(MongoCollectionConstants.USERS));
			updatesPerCollection.get(MongoCollectionConstants.USER_MONTHLY_BALANCE).addAll( settleOpenBet.get(MongoCollectionConstants.USER_MONTHLY_BALANCE));
		
		}

		
		MongoTransactionalBlock<Void> mongoBlock = new MongoTransactionalBlock<Void>() {
			
			@Override
			public void begin() throws Exception {	
				
				//TODO :: Cocnurrent modufucation!!!
				
				if (! updatesPerCollection.get(MongoCollectionConstants.USER_BET_PREDICTIONS).isEmpty()) {					
					BulkWriteResult bulkWrite = predictionsCollection.bulkWrite(session, updatesPerCollection.get(MongoCollectionConstants.USER_BET_PREDICTIONS));
					logger.info(Thread.currentThread().getName() + " bet pred upd: " + bulkWrite);
				}
				
				if (! updatesPerCollection.get(MongoCollectionConstants.USER_BETS).isEmpty()) {					
					BulkWriteResult bulkWrite = betsCollection.bulkWrite(session, updatesPerCollection.get(MongoCollectionConstants.USER_BETS));
					logger.info(Thread.currentThread().getName() + " bet upd: " + bulkWrite);
				}
				
				if (! updatesPerCollection.get(MongoCollectionConstants.USERS).isEmpty()) {					
					BulkWriteResult bulkWrite = usersCollection.bulkWrite(session, updatesPerCollection.get(MongoCollectionConstants.USERS));
					logger.info(Thread.currentThread().getName() + " bet user upd: " + bulkWrite);
				}
				
				if (! updatesPerCollection.get(MongoCollectionConstants.USER_MONTHLY_BALANCE).isEmpty()) {					
					BulkWriteResult bulkWrite = balancesCollection.bulkWrite(session, updatesPerCollection.get(MongoCollectionConstants.USER_MONTHLY_BALANCE));
					logger.info(Thread.currentThread().getName() + " balaces user upd: " + bulkWrite);
				}
				
			}
		};
		
		mongoBlock.setRetries(5);
		boolean updated = mongoBlock.execute();
				

		System.out.println(Thread.currentThread().getName() +  " SETTLED BETS " + updated);
		
		return updated;
				
		
    }
	
	
	@Override
	public void validateUser(String email) {
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				Document userFilter = new Document(MongoFields.EMAIL, email);
				Document validDoc = new Document(MongoFields.VALIDATED, true);
				Document pushDocument = new Document("$set", validDoc);
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
				usersCollection.findOneAndUpdate(session, userFilter, pushDocument);
			}
		}.execute();
	}

	@Override
	public void deleteUser(String mongoId) {
		boolean deleted = new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				Bson userMongoIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoId));
				Bson userBetFilter = Filters.eq(MongoFields.MONGO_USER_ID, mongoId);

				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);
				MongoCollection<Document> balancesCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);

				predictionsCollection.deleteMany(userBetFilter);
				betsCollection.deleteMany(userBetFilter);
				balancesCollection.deleteMany(userBetFilter);
				usersCollection.deleteOne(userMongoIdFilter);
			}
		}.execute();
		
		System.out.println("DELETED " +deleted+ "::" + mongoId);
	}
	
	
	public FindIterable<Document> getFilteredBets(Bson betsFilter){
		if (betsFilter == null) {
			throw new RuntimeException("Cannot retrieve all bets, please filter");
		}
		
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
		return betsCollection.find(betsFilter);
	}
	
	public boolean settleDelayedOpenPredictions(Set<OpenBetDelayedLeagueDatesBean> toHandle) {
		for (OpenBetDelayedLeagueDatesBean openBetDelayedLeagueDatesBean : toHandle) {
			for (String date : openBetDelayedLeagueDatesBean.getMissingDates()) {
				
				try {
					Events leagueEventsByDate = SportScoreClient.getLeagueEventsByDate(openBetDelayedLeagueDatesBean.getLeagueId(), date);
					if (leagueEventsByDate == null || leagueEventsByDate.getData() == null || leagueEventsByDate.getData().isEmpty()) {
						System.out.println("NO DELAYED EVENTS FOUND!!!!!!!!!!!");
						continue;
					}
					
					for (MatchEvent incomingEvent : leagueEventsByDate.getData()) {
						System.out.println("DELAYED EVENT FOUND:::" + incomingEvent.getId());
						
						FootballApiCache.checkForCaching(incomingEvent);
						
					
					}
					
				} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		return false;
	}

	
	
	 boolean closeMonthlyBalances(ClientSession session) {
			int nextMonth = DateUtils.getMonthAsInt(1);

			Bson nextMonthDoc = Filters.eq(MongoFields.USER_BALANCE_MONTH, nextMonth);
			MongoCollection<Document> userBalanceCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
			userBalanceCollection.updateMany(session, nextMonthDoc, // filter, apply to all documents

					new Document("$set", new Document(MongoFields.USER_BALANCE, ServerConstants.STARTING_BALANCE))

			);

			return true;
		
	}
	 
	 boolean commonChecksToCloseMonth(ClientSession session) {
		 	int previousMonth = DateUtils.getMonthAsInt(-1);
			int previousYear = DateUtils.getYearOfPreviousMonthAsInt();

			
			Bson monthlyWinnerMonthFilter = Filters.eq(MongoFields.AWARD_MONTH, previousMonth);
			Bson monthlyWinnerYearFilter = Filters.eq(MongoFields.AWARD_YEAR, previousYear);
			Bson combined = Filters.and(monthlyWinnerMonthFilter, monthlyWinnerYearFilter);
			
			long existingWinner = fetchFilterSize(MongoCollectionConstants.AWARDS, combined);

			if(existingWinner>0) {
				CommonLogger.logger.info("Monthly winner exists");
				return false;
			}
			
			Bson openBetsOfPreviousMonthFilter = Filters.eq(MongoFields.BET_BELONGING_MONTH, previousMonth);
			Bson openBetsOfPreviousYearFilter = Filters.eq(MongoFields.BET_BELONGING_YEAR, previousYear);
			Bson openBetsOfPreviousMonthYearFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
			
			Bson combinedFilter = Filters.and(openBetsOfPreviousMonthFilter, openBetsOfPreviousMonthYearFilter, openBetsOfPreviousYearFilter);
			long openBets = fetchFilterSize(MongoCollectionConstants.USER_BETS, combinedFilter);
			
			if(openBets > 0) {
				CommonLogger.logger.info(openBets + " open bets still in place for " + previousMonth);
				return false;
			}
		
			
			return true;
	 }
			
	
	/**
	 * 1. All bets of previous month must be settled.
	 * 2. There must not exist a monthly winner record for the previous month.
	 */
	boolean computeMonthWinner(ClientSession session) {
		int previousMonthInt = DateUtils.getMonthAsInt(-1);
		int yearOfPreviousMonthInt = DateUtils.getYearOfPreviousMonthAsInt();
			
		//at this point all user balances have been reset.
		
		MongoCollection<Document> userBalanceCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
		
        Bson max =   Aggregates.group(null, Accumulators.max("maxBalance", "$"+MongoFields.USER_BALANCE));
        Bson month =   Filters.eq(MongoFields.USER_BALANCE_MONTH, previousMonthInt);
        

        AggregateIterable<Document> result = userBalanceCollection.aggregate(Arrays.asList(
                Aggregates.match(month), // Filter for documents where columnB = 1
                max, // Get max value of columnA
                Aggregates.project(Projections.fields(Projections.include("maxBalance"))) // Optional projection
            ));
        
		Double maxValue = - 1d;
		
		// Retrieve and print the result
		for (Document doc : result) {
			// Get the maximum value
			maxValue = doc.getDouble("maxBalance");
			System.out.println("Max value: " + maxValue);
		}
		
		Bson balanceFilter = Filters.gt(MongoFields.USER_BALANCE_LEADERBOARD, maxValue - 0.2);
		Bson combinedBalanceMonthYear = Filters.and(balanceFilter, month);

		Bson matchMaxBalance = Aggregates.match(combinedBalanceMonthYear);
		List<Bson> asList = Arrays.asList(matchMaxBalance);
		
		AggregateIterable<Document> aggregate = userBalanceCollection.aggregate(asList);
				
		List<Document> awardsDocs  = new ArrayList<>();
		for (Document record : aggregate) {
			Document awardDocument = MongoUtils.getAwardDocument(record.getString(MongoFields.MONGO_USER_ID).toString(), maxValue, previousMonthInt, yearOfPreviousMonthInt);
//			System.out.println(record.toJson());
			awardsDocs.add(awardDocument);
		}    
		
		if(awardsDocs.isEmpty()) {
			Document awardDocument = MongoUtils.getAwardDocument("-1", 0d, previousMonthInt, yearOfPreviousMonthInt);
			awardsDocs.add(awardDocument);
			CommonLogger.logger.error("No winner found for " + previousMonthInt);
		}
		
		MongoCollection<Document> awardsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.AWARDS);

		awardsCollection.insertMany(session, awardsDocs);
		
		return true;	
	}
	
	
	
	List<UserPurchase> getUserMonthlyPurchases(User user) {
		
		MongoTransactionalBlock<List<UserPurchase>> purchasesBlock = new MongoTransactionalBlock<List<UserPurchase>>() {

			@Override
			public void begin() throws Exception {
				result = null;
				int belongingMonth = LocalDate.now().getMonthValue();
				int belongingYear = LocalDate.now().getYear();
				
				Bson filterUser = Filters.eq(MongoFields.MONGO_USER_ID, user.getMongoId());
				Bson filterMonth = Filters.eq(MongoFields.PURCHASE_MONTH, belongingMonth);
				Bson filterYear = Filters.eq(MongoFields.PURCHASE_YEAR, belongingYear);
				Bson filterUserWithMonthYear = Filters.and(filterUser, filterMonth, filterYear);
				
				Executor<UserPurchase> betPurchasesExecutor = new Executor<UserPurchase>(
						new TypeToken<UserPurchase>() {});
				result = MongoUtils.get(session, MongoCollectionConstants.USER_PURCHASES,
						filterUserWithMonthYear, betPurchasesExecutor);
				
				
			}
			
		};
		
		purchasesBlock.execute();
		
		return purchasesBlock.getResult();
	}
	
	/**
	 * Collects the documents to update per collection.
	 * No update executed here.
	 * 
	 * @param betDocument
	 * @param usersCollection
	 * @param predictionsCollection
	 * @param betsCollection
	 * @return
	 * @throws Exception
	 */
	Map<String, List<WriteModel<Document>>> settleOpenBet( Document betDocument, 
			MongoCollection<Document> predictionsCollection) throws Exception {
		
		
		Map<String, List<WriteModel<Document>>> updatesPerCollection = new HashMap<>();
		updatesPerCollection.put(MongoCollectionConstants.USER_BET_PREDICTIONS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USER_BETS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USERS, new ArrayList<>());
		updatesPerCollection.put(MongoCollectionConstants.USER_MONTHLY_BALANCE, new ArrayList<>());
		
		String betId = betDocument.getObjectId(MongoFields.MONGO_ID).toString();
		String userId = betDocument.getString(MongoFields.MONGO_USER_ID).toString();
		int betMonth = betDocument.getInteger(MongoFields.BET_BELONGING_MONTH);
		
		Bson betPredictionsByBetIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, betId);
		FindIterable<Document> betPredictions = predictionsCollection.find(betPredictionsByBetIdFilter);
		
		if (!betPredictions.iterator().hasNext()) {
			System.out.println("******************* NO PREDS ERROR BET " + betId);
			return updatesPerCollection;
			
		}
		
		int allPredictions = 0;
		int pendingPredictionsCount = 0;
		int lostPredictionsCount = 0;
		int wonPredictionsCount = 0;
		
		int alreadySettledLostPredictionsCount = 0;
		int alreadySettledWonPredictionsCount = 0;
		int alreadySettledWithdrawnPredictionsCount = 0;
		
		List<Bson> betUpdateDocuments = new ArrayList<>();//what to update in UserBet collection
		List<Bson> userUpdateDocuments = new ArrayList<>();//what to update in User collection
		List<Bson> userBalanceUpdateDocuments = new ArrayList<>();//what to update in User month balance collection
		List<Bson> predictionsToBeSettledDocuments = new ArrayList<>();//what to update in Predictions collection
		
		
		double possibleWinnings = betDocument.getDouble(MongoFields.BET_AMOUNT);
		for (Document predictionDocument : betPredictions) {
			allPredictions = allPredictions + 1;
			
			int predictionStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_STATUS);
			int predictionSettleStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS);
			
			if (PredictionSettleStatus.SETTLED.getCode() == predictionSettleStatus) {// this means it has been handled on a previous run
				if (PredictionStatus.MISSED.getCode() == predictionStatus) {
					alreadySettledLostPredictionsCount = alreadySettledLostPredictionsCount + 1;
				}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
					
					possibleWinnings = possibleWinnings * predictionDocument.getDouble(MongoFields.USER_BET_PREDICTION_ODD_VALUE);
					
					alreadySettledWonPredictionsCount = alreadySettledWonPredictionsCount + 1;
				}else if (PredictionStatus.WITHDRAWN.getCode() == predictionStatus){
					alreadySettledWithdrawnPredictionsCount = alreadySettledWithdrawnPredictionsCount + 1;
				}
			}else if (PredictionStatus.PENDING.getCode() == predictionStatus) {
				pendingPredictionsCount = pendingPredictionsCount + 1;
			}else if (PredictionStatus.MISSED.getCode() == predictionStatus) {
				lostPredictionsCount = lostPredictionsCount + 1;
				predictionsToBeSettledDocuments.add(Filters.eq(MongoFields.MONGO_ID, predictionDocument.getObjectId(MongoFields.MONGO_ID)));
			}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
				
				possibleWinnings = possibleWinnings * predictionDocument.getDouble(MongoFields.USER_BET_PREDICTION_ODD_VALUE);
				
				wonPredictionsCount = wonPredictionsCount + 1;
				predictionsToBeSettledDocuments.add(Filters.eq(MongoFields.MONGO_ID, predictionDocument.getObjectId(MongoFields.MONGO_ID)));
			}
		}
		
		
		BetStatus betStatus = BetStatus.fromCode(betDocument.getInteger(MongoFields.BET_STATUS, 0));
		if (pendingPredictionsCount > 0 ) {
			
			if (lostPredictionsCount > 0 && ! BetStatus.PENDING_LOST.equals(betStatus)) {
				Bson setLostFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
				betUpdateDocuments.add(setLostFilter);
			}
			
		}else {
			
			if (lostPredictionsCount == 0 && alreadySettledLostPredictionsCount == 0 
					&& wonPredictionsCount > 0 && (wonPredictionsCount+alreadySettledWonPredictionsCount+alreadySettledWithdrawnPredictionsCount == allPredictions)) {
				
				// WE HAVE A WINNER!!!
				
				Bson incUserBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, possibleWinnings);
				Bson increaseWonMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_SLIPS, 1);
			     
				System.out.println("IN THREAD BET WILL INCREASE BALANCE FOR "+userId+":::" + possibleWinnings);
								
				userBalanceUpdateDocuments.add(incUserBalanceDocument);
				userBalanceUpdateDocuments.add(increaseWonMonthlyBetsDocument);

				Bson increaseWonOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_SLIPS, 1);
				userUpdateDocuments.add(increaseWonOverallBetsDocument);
				
				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_FAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);
				
			}else if (lostPredictionsCount > 0 || alreadySettledLostPredictionsCount > 0){

				Bson increaseLostOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_SLIPS, 1);
				userUpdateDocuments.add(increaseLostOverallBetsDocument);

				Bson increaseLostMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_SLIPS, 1);
				userBalanceUpdateDocuments.add(increaseLostMonthlyBetsDocument);

				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_UNFAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);
				
			}else if (lostPredictionsCount == 0 && wonPredictionsCount == 0 && alreadySettledWithdrawnPredictionsCount > 0 && alreadySettledWithdrawnPredictionsCount == allPredictions){

				Bson incUserBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, betDocument.getDouble(MongoFields.BET_AMOUNT));
				Bson setWithdrawnFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_WITHDRAWN.getCode());
				
				betUpdateDocuments.add(setWithdrawnFilter);
				
				userBalanceUpdateDocuments.add(incUserBalanceDocument);
				
			}else {
				throw new RuntimeException();
			}
		
		}

		if (wonPredictionsCount > 0) {
			Bson increaseWonOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_EVENTS, wonPredictionsCount);
			Bson increaseWonMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_EVENTS, wonPredictionsCount);

			userUpdateDocuments.add(increaseWonOverallPredictionsDocument);
			userBalanceUpdateDocuments.add(increaseWonMonthlyPredictionsDocument);
		}

		
		if (lostPredictionsCount > 0) {
			Bson increaseLostOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_EVENTS, lostPredictionsCount);
			Bson increaseLostMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_EVENTS, lostPredictionsCount);
			
			userUpdateDocuments.add(increaseLostOverallPredictionsDocument);
			userBalanceUpdateDocuments.add(increaseLostMonthlyPredictionsDocument);
		}

		
		
		if (!predictionsToBeSettledDocuments.isEmpty()) {
			Bson settledUpdate = Updates.set(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, PredictionSettleStatus.SETTLED.getCode());
			//predictionsCollection.updateMany(session, Filters.or(predictionsToBeSettledDocuments), settledUpdate);
			
			UpdateManyModel<Document> updateSettledPredictionsDocuments = new UpdateManyModel<>(
					Filters.or(predictionsToBeSettledDocuments),
					settledUpdate
			        );
			
			updatesPerCollection.get(MongoCollectionConstants.USER_BET_PREDICTIONS).add(updateSettledPredictionsDocuments);
			
		}
		
		if (!betUpdateDocuments.isEmpty()) {
			Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
			
			
			System.out.println("*************** SETTLING BET "+ betId + " with " + betUpdateDocuments.size());
			
			
				
				UpdateOneModel<Document> updatebetDocument = new UpdateOneModel<>(
						betByIdFilter,
						Updates.combine(betUpdateDocuments)
						);
				
				updatesPerCollection.get(MongoCollectionConstants.USER_BETS).add(updatebetDocument);
			
			
			
		}
		
		if (!userBalanceUpdateDocuments.isEmpty()) {
			Bson monthBalanceFilter = Filters.eq(MongoFields.USER_BALANCE_MONTH, betMonth);
			Bson userIdBalanceFilter = Filters.eq(MongoFields.MONGO_USER_ID, userId);
			Bson monthAndUserIdFilter = Filters.and(monthBalanceFilter, userIdBalanceFilter);// all balance updates will use this.
			
			UpdateOneModel<Document> updateOneBalanceModel = new UpdateOneModel<>(
                    monthAndUserIdFilter,  // Filter for document where name is "John"
                    Updates.combine(userBalanceUpdateDocuments)
                );
			
			updatesPerCollection.get(MongoCollectionConstants.USER_MONTHLY_BALANCE).add(updateOneBalanceModel);
			
		}
		
		if (lostPredictionsCount > 0 || wonPredictionsCount > 0) {
			Bson userIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betDocument.getString(MongoFields.MONGO_USER_ID)));
			
			UpdateOneModel<Document> updateUserDocument = new UpdateOneModel<>(
					userIdFilter,
					Updates.combine(userUpdateDocuments)
			        );
			
			updatesPerCollection.get(MongoCollectionConstants.USERS).add(updateUserDocument);
			
		}
		
		return updatesPerCollection;
	}
	
	
	public long fetchFilterSize(String collection, Bson filter) {
		MongoCollection<Document> mongoCollection = MongoUtils.getMongoCollection(collection);		
		return mongoCollection.countDocuments(filter);
	}
	
	public Bson pendingOrPendingLostBetsFilter() {
		Bson pendingBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
		Bson pendingLostBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
		Bson pendingOrPendingLostFilter = Filters.or(pendingBetsFilter, pendingLostBetsFilter);
		return pendingOrPendingLostFilter;
	}
	
	
	User userFromMongoDocument(Document userFromMongo) {
		if (userFromMongo == null) {
			return null;
		}

		String userString = userFromMongo.toJson();
		User finalUser = new Gson().fromJson(userString, new TypeToken<User>() {
		}.getType());
		String mongoId = userFromMongo.getObjectId(MongoFields.MONGO_ID).toString();

		finalUser.setMongoId(mongoId);
		finalUser.setPassword(null);
		
//		System.out.println("getting user " + mongoId);

		double overallBetAmount = userFromMongo.getDouble(MongoFields.USER_BET_AMOUNT_OVERALL);
		finalUser.setOverallBetAmount(overallBetAmount);
		finalUser.setOverallLostEventsCount(userFromMongo.getInteger(MongoFields.USER_OVERALL_LOST_EVENTS));
		finalUser.setOverallWonEventsCount(userFromMongo.getInteger(MongoFields.USER_OVERALL_WON_EVENTS));
		finalUser.setOverallLostSlipsCount(userFromMongo.getInteger(MongoFields.USER_OVERALL_LOST_SLIPS));
		finalUser.setOverallWonSlipsCount(userFromMongo.getInteger(MongoFields.USER_OVERALL_WON_SLIPS));
		
		return finalUser;
	}
	
	/**
	 * Every bet has a field for the {@link User#getMongoId()} that holds it.
	 * 
	 * Fetch bets for the last 3 days only.
	 * 
	 * @param userId
	 * @param millisToSearchBets 
	 * @param maxBetsToFetch 
	 * @return
	 */
	List<UserBet> getBetsForUser(ClientSession session, String userId, int maxBetsToFetch, long millisToSearchBets, boolean includeUnsettled) {
		Bson userBetsFilter = Filters.eq(MongoFields.MONGO_USER_ID, userId);
		Bson betMillisFilter = Filters.gt(MongoFields.USER_BET_PLACEMENT_MILLIS, System.currentTimeMillis() - millisToSearchBets);
		Bson excludeStatusesFilter = new Document();
		if (!includeUnsettled) {
			excludeStatusesFilter = Filters.nin(MongoFields.BET_STATUS, BetStatus.PENDING.getCode(), BetStatus.PENDING_LOST.getCode());
		}

		Bson userBetsUpToThreeDaysBeforeFilter = Filters.and(userBetsFilter, betMillisFilter, excludeStatusesFilter);
		Executor<UserBet> betsExecutor = new Executor<UserBet>(new TypeToken<UserBet>() {});
		Document sortFilter = new Document(MongoFields.USER_BET_PLACEMENT_MILLIS, -1);
		List<UserBet> bets = MongoUtils.getSorted(session, MongoCollectionConstants.USER_BETS, betsExecutor, userBetsUpToThreeDaysBeforeFilter, sortFilter, maxBetsToFetch);

		for (UserBet bet : bets) {
			Bson userBetPredictionsFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID,
					bet.getMongoId());
			Executor<UserPrediction> betPredictionsExecutor = new Executor<UserPrediction>(
					new TypeToken<UserPrediction>() {});
			List<UserPrediction> betPredictions = MongoUtils.get(session, MongoCollectionConstants.USER_BET_PREDICTIONS,
					userBetPredictionsFilter, betPredictionsExecutor);
			bet.setPredictions(betPredictions);
			
			betPredictions.forEach(bp -> findTeams(session, bp));
		}

		return bets;
	}

	void findTeams(ClientSession session, UserPrediction p) {
		MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(p.getEventId());
		if (matchEvent != null) {
			p.setHomeTeam(matchEvent.getHome_team());
			p.setAwayTeam(matchEvent.getAway_team());
			return;
		}
		
		Team homeTeam = FootballApiCache.ALL_TEAMS.get(p.getHomeTeamId());
		Team awayTeam = FootballApiCache.ALL_TEAMS.get(p.getAwayTeamId());
		if (homeTeam != null && awayTeam != null) {
			p.setHomeTeam(homeTeam);
			p.setAwayTeam(awayTeam);
			return;
		}
		
		Bson homeTeamFilter = Filters.eq(MongoFields.ID, p.getHomeTeamId());
		Bson awayTeamFilter = Filters.eq(MongoFields.ID, p.getAwayTeamId());
		Bson orFilter = Filters.or(homeTeamFilter, awayTeamFilter);
		Executor<Team> teamExecutor = new Executor<Team>(new TypeToken<Team>() {});
		List<Team> teams = MongoUtils.get(session, MongoCollectionConstants.TEAM, orFilter, teamExecutor);
		
		if (teams.size() != 2) {
			Team mockTeam = new Team();
			mockTeam.setName("Missing team");
			//mockTeam.setLogo("https://xscore.cc/resb/team/asteras-tripolis.png");
			mockTeam.setId(-1);
			
			p.setHomeTeam(mockTeam);
			p.setAwayTeam(mockTeam);
		}else {
			for (Team team : teams) {
				if (p.getHomeTeamId() == team.getId()) {
					p.setHomeTeam(team);
				}
				
				if (p.getAwayTeamId() == team.getId()) {
					p.setAwayTeam(team);
				}
			}
		}
	
		
	}
	

	public List<Section> getSectionsFromDb(ClientSession session) {
		Executor<Section> sectionsExecutor = new Executor<Section>(new TypeToken<Section>() {});
		List<Section> sections = MongoUtils.get(session, MongoCollectionConstants.SECTIONS, new Document(), sectionsExecutor);
		return sections;
	}
	
	public List<League> getLeaguesFromDb(ClientSession session) {
		Executor<League> leaguesExecutor = new Executor<League>(new TypeToken<League>() {});
		List<League> leagues = MongoUtils.get(session, MongoCollectionConstants.LEAGUES, new Document(), leaguesExecutor);
		return leagues;
	}
	
	public void updateSections(List<Section> incoming) {
		List<Document> sectionDocuments = new ArrayList<>();
		List<Section> newSections = new ArrayList<>();
		for (Section section : incoming) {
			if (MongoUtils.DB_DATA_FETCHED && !FootballApiCache.ALL_SECTIONS.containsKey(section.getId()) 
					&& FootballApiCache.SUPPORTED_SECTION_IDS.containsKey(section.getId())) {
				Document sectionDocument = MongoUtils.getSectionDocument(section);
				sectionDocuments.add(sectionDocument);
				newSections.add(section);
			}
		}
		
		if (!sectionDocuments.isEmpty()) {
			boolean inserted = new MongoTransactionalBlock() {
				
				@Override
				public void begin() throws Exception {
					MongoCollection<Document> sectionsColl = MongoUtils.getMongoCollection(MongoCollectionConstants.SECTIONS);
					sectionsColl.insertMany(session, sectionDocuments);
				}
			}.execute();
		
		if (inserted) {
			for (Section s : newSections) {
				FootballApiCache.ALL_SECTIONS.put(s.getId(), s);
			}
			
			System.out.println("INSERTED SECTIONS:::" + newSections.size());
		}
		
		}
	}
	
	public void updateLeagues(List<League> incoming) {
		List<Document> leagueDocuments = new ArrayList<>();
		List<League> newLeagues = new ArrayList<>();
		for (League league : incoming) {
			if (MongoUtils.DB_DATA_FETCHED && !FootballApiCache.ALL_LEAGUES.containsKey(league.getId())
					&& FootballApiCache.SUPPORTED_SECTION_IDS.containsKey(league.getSection_id())
					&& FootballApiCache.SUPPORTED_SECTION_IDS.get(league.getSection_id()).contains(league.getId())) {
				//TODO: add supported league ids check!
				Document leagueDocument = MongoUtils.getLeagueDocument(league);
				leagueDocuments.add(leagueDocument);
				newLeagues.add(league);
			}
		}
		
		if (!leagueDocuments.isEmpty()) {
			boolean inserted = new MongoTransactionalBlock() {
				
				@Override
				public void begin() throws Exception {
					MongoCollection<Document> leaguesColl = MongoUtils.getMongoCollection(MongoCollectionConstants.LEAGUES);
					leaguesColl.insertMany(session, leagueDocuments);
				}
			}.execute();
		
		if (inserted) {
			for (League s : newLeagues) {
				FootballApiCache.ALL_LEAGUES.put(s.getId(), s);
			}
			
			System.out.println("INSERTED LEAGUES:::" + newLeagues.size());
		}
		
		}
	}
	
	
	List<UserAward> getAwardsForUser(ClientSession session, String userId) {
		Bson userAwardsFilter = Filters.eq(MongoFields.MONGO_USER_ID, userId);
		Executor<UserAward> awardsExecutor = new Executor<UserAward>(new TypeToken<UserAward>() {});
		List<UserAward> awards = MongoUtils.get(session, MongoCollectionConstants.AWARDS, userAwardsFilter, awardsExecutor);
		return awards;
	}
	
	List<UserMonthlyBalance> getBalancesForUser(ClientSession session, String userId) {
		Bson userBalancesFilter = Filters.eq(MongoFields.MONGO_USER_ID, userId);
		Executor<UserMonthlyBalance> balancesExecutor = new Executor<UserMonthlyBalance>(new TypeToken<UserMonthlyBalance>() {});
		List<UserMonthlyBalance> balances = MongoUtils.get(session, MongoCollectionConstants.USER_MONTHLY_BALANCE, userBalancesFilter, balancesExecutor);
		return balances;
	}
	
	long userPosition(ClientSession session, User user){
		Bson greaterFromUserBalance = Filters.gt(MongoFields.USER_BALANCE, user.getBalance());
		Bson thisMonthBalance = Filters.eq(MongoFields.USER_BALANCE_MONTH, DateUtils.getMonthAsInt(0));
		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
		return usersCollection.countDocuments(session, Filters.and(thisMonthBalance, greaterFromUserBalance)) + 1;
	}
	
	UserLevel getUserLevel(User user) {
		return UserLevel.from(user.getOverallWonSlipsCount() + user.getOverallLostSlipsCount(), user.getOverallWonSlipsCount(), 
				user.getOverallWonEventsCount(), user.getBounties().size(), user.getUserAwards().size());
	}


	boolean liveGameInPredictions(List<UserPrediction> predictions) {
		for (UserPrediction userPrediction : predictions) {
			MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(userPrediction.getEventId());

			//TODO: We can get in trouble with postponed matches if they neveer start
			if (!MatchEventStatus.NOTSTARTED.getStatusStr().equals(matchEvent.getStatus()) 
				&& !MatchEventStatus.DELAYED.getStatusStr().equals(matchEvent.getStatus())
				&& !MatchEventStatus.POSTPONED.getStatusStr().equals(matchEvent.getStatus())){
				return true;
			}
		}
		
		return false;
	}

	public List<Team> getTeamsFromDb(ClientSession session) {
		Executor<Team> teamsExecutor = new Executor<Team>(new TypeToken<Team>() {});
		List<Team> teams = MongoUtils.get(session, MongoCollectionConstants.TEAM, new Document(), teamsExecutor);
		return teams;
	}

	public void updateTeams(Set<Team> teams) {
		List<Document> teamsDoc = new ArrayList<>();
		for (Team team : teams) {
			if (FootballApiCache.ALL_TEAMS.containsKey(team.getId()) || team.getId() == 0) {
				continue;
			}
			
			teamsDoc.add(MongoUtils.getTeamDocument(team));
			
		}
		
		if (!teamsDoc.isEmpty()) {
			
			boolean inserted = new MongoTransactionalBlock<Void>() {

				@Override
				public void begin() throws Exception {
					MongoCollection<Document> teamsCol = MongoUtils.getMongoCollection(MongoCollectionConstants.TEAM);
					teamsCol.insertMany(session, teamsDoc);
				}
			}.execute();
			
			if (inserted) {
				teams.forEach(t -> FootballApiCache.ALL_TEAMS.put(t.getId(), t));
			}
		}
	}

	

}
