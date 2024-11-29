package gr.server.impl.client;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;

import gr.server.common.CommonConstants;
import gr.server.common.ServerConstants;
import gr.server.common.StringConstants;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionSettleStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.enums.UserLevel;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserAward;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.def.client.MongoClientHelper;
import gr.server.logging.Mongo;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class MongoClientHelperImpl 
implements MongoClientHelper {
	
	static Logger logger = Mongo.logger;

	
	@Override
	public User createUser(User user) {
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				System.out.println("Create user Working in thread: " + Thread.currentThread().getName());
				MongoClient client = MongoUtils.getMongoClient();
				MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
						.getCollection(CollectionNames.USERS);

				Document existingUserName = new Document(MongoFields.USERNAME, user.getUsername());
				Document existingEmail = new Document(MongoFields.EMAIL, user.getEmail());
				List<Document> filters = new ArrayList<>();
				filters.add(existingEmail);
				filters.add(existingUserName);
				Document orDocument = MongoUtils.getAndOrDocument(filters, MongoFields.OR);

				FindIterable<Document> find = users.find(orDocument);
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
				users.insertOne(newUserDocument);

				user.setMongoId(newUserDocument.getObjectId(MongoFields.MONGO_ID).toString());
				user.init();
				user.setUsername(newUserDocument.getString(MongoFields.USERNAME));
				user.setEmail(newUserDocument.getString(MongoFields.EMAIL));
				// createdUser.setPassword(newUser.getString(Fields.PASSWORD));
				// createdUser.setPosition(SyncHelper.userPosition(createdUser));//TODO later
			}
		}.execute();
		
		return user;
		
	}

	@Override
	public User loginUser(User user) {
		MongoClient client = MongoUtils.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);

		Document existingUserName = new Document(MongoFields.USERNAME, user.getUsername());
		Document existingEmail = new Document(MongoFields.EMAIL, user.getEmail());
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = MongoUtils.getAndOrDocument(filters, MongoFields.OR);

		FindIterable<Document> find = users.find(orDocument);
		Document existingUser = find.first();
		if (existingUser != null) {
			boolean isValid = existingUser.getBoolean(MongoFields.VALIDATED);
			if (!isValid) {
				user.setErrorMessage(user.getEmail() + " needs to validate email");
			} else if (!user.getPassword().trim().equals(existingUser.getString(MongoFields.PASSWORD))) {
				user.setErrorMessage("Wrong username or password");
			} else {
				user = userFromMongoDocument(existingUser);
			}

		} else {
			user.setErrorMessage("User not found");
		}

		return user;
	}

	@Override
	public User getUser(String mongoId) {
		MongoCollection<Document> users = MongoUtils.getMongoCollection(CollectionNames.USERS);
		Bson userMongoIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoId));
		FindIterable<Document> usersFromMongo = users.find(userMongoIdFilter);

		Document userFromMongo = usersFromMongo.first();
		if (userFromMongo == null) {
			return null;
		}

		User user = userFromMongoDocument(userFromMongo);
		user.setUserBets(getBetsForUser(mongoId));
		user.setUserAwards(getAwardsForUser(mongoId));
		user.setBounties(new ArrayList<>());
		user.setLevel(getUserLevel(user).getCode());

		return user;
	}
	
	@Override
	public Set<MatchEvent> getLiveByIds(String ids) {
		// TODO in the future maybe call SportScore??
		if (ids == null || ids.trim().length() == 0) {
			return new HashSet<>();
		}
		
		String[] eventIds = ids.trim().replace(StringConstants.EMPTY, StringConstants.EMPTY)
			.split(StringConstants.COMMA);
		
		Set<MatchEvent> favs = new HashSet<>();
		for (String eventId : eventIds) {
			if (eventId.equals(StringConstants.EMPTY)) {
				continue;
			}
			
			MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(Integer.parseInt(eventId));
			if (matchEvent != null) {
				favs.add(matchEvent);
			}
		}
		
		return favs;
	}
	
	@Override
	public BetPlacementStatus placeBet(UserBet userBet) {
		
		if(userBet.getBetAmount()<=0 || insufficientFundsFor(userBet)) {
			return BetPlacementStatus.FAILED_INSUFFICIENT_FUNDS;	
		}
		
		if (liveGameInPredictions(userBet.getPredictions())) {
			return BetPlacementStatus.FAILED_MATCH_IN_PROGRESS;
		}
		
		if (predictionsSpanNextMonth(userBet.getPredictions())) {
			return BetPlacementStatus.FAIL_GENERIC;
		}
		
		boolean inserted = new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				
				userBet.setBetStatus(BetStatus.PENDING);
				userBet.setBetPlaceMillis(System.currentTimeMillis());
				
				userBet.setBelongingMonth(LocalDate.now().getMonthValue());//TODO what if some events span to another month?
				userBet.setBelongingYear(LocalDate.now().getYear());

				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
				Document newBet = MongoUtils.getBetDocument(userBet);
				betsCollection.insertOne(session, newBet);
				
				String newBetMongoId = newBet.getObjectId(MongoFields.MONGO_ID).toString();
				
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
				
				Runnable storeTeamsTask = () -> { userBet.getPredictions().forEach(p -> storeTeams(p.getEventId())); };
				ScheduledExecutorService storeTeamsExecutor = Executors.newSingleThreadScheduledExecutor();
				storeTeamsExecutor.schedule(storeTeamsTask, 3l, TimeUnit.SECONDS);
				
								
				List<Document> newPredictions = MongoUtils.getPredictionsDocuments(userBet, newBetMongoId);
				predictionsCollection.insertMany(session, newPredictions);
				
				userBet.setMongoId(newBetMongoId);
				
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
				Bson filter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoUserId()));
				Bson increaseBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, (-1 * userBet.getBetAmount()));
				usersCollection.findOneAndUpdate(session, filter, increaseBalanceDocument);
				
			}

			//TODO: this should be done in the background,dont care about failure
			private void storeTeams(int eventId) {
				MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(eventId);
				Map<Integer, Team> teamsToInsert = new HashMap<>();
				teamsToInsert.put(matchEvent.getHome_team().getId(), matchEvent.getHome_team());
				teamsToInsert.put(matchEvent.getAway_team().getId(), matchEvent.getAway_team());
				MongoCollection<Document> teamsCollection = MongoUtils.getMongoCollection(CollectionNames.TEAM);
				Bson homeTeamFilter = Filters.eq(MongoFields.TEAM_ID, matchEvent.getHome_team().getId());
				Bson awayTeamFilter = Filters.eq(MongoFields.TEAM_ID, matchEvent.getAway_team().getId());
				Bson orFilter = Filters.or(homeTeamFilter, awayTeamFilter);
				FindIterable<Document> teamsDocuments = teamsCollection.find(orFilter);
				
				for (Document document : teamsDocuments) {
					Integer teamId = document.getInteger(MongoFields.TEAM_ID);
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
			return BetPlacementStatus.PLACED;
		}
		
		return BetPlacementStatus.FAIL_GENERIC;
	}

	private boolean predictionsSpanNextMonth(List<UserPrediction> predictions) {
		for (UserPrediction userPrediction : predictions) {
			
			MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(userPrediction.getEventId());
			if (DateUtils.isInNextMonth(matchEvent.getStart_at())) {
				System.out.println("**********************MATCH IN NEXT MONTH");
				return true;
			}
		}
		
		return false;
	}

	private boolean insufficientFundsFor(UserBet userBet) {
		User user = new MongoClientHelperImpl().getUser(userBet.getMongoUserId());
		return user.getBalance() - userBet.getBetAmount() < 0;
	}

	public List<User> retrieveLeaderBoard() {
		Document sortField = new Document(MongoFields.USER_BALANCE, -1);
		Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() {});
		List<User> users = MongoUtils.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 20);
		users.forEach(u -> 
		{ 
			//TODO this will be costly - consider mongo fields
			u.setBounties(new ArrayList<>());
			u.setUserAwards(new ArrayList<>());
			u.setLevel(getUserLevel(u).getCode());
		
		}
			
		);
		return users;
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

//		List<MatchEvent> todaysMatches = FootballApiCache.ALL_EVENTS.values().stream().collect(Collectors.toList());// ApiDataFetchHelper.eventsForDate(new Date());
//		
//		Set<MatchEvent> todaysFinishedEvents = todaysMatches.stream().filter(
//							match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus()))
//					.collect(Collectors.toSet());
//		
//		
//		Set<Integer> settledEventsIds = getTodaySettledEvents();
		
		
        List<UpdateManyModel<Document>> bulkOperations = new ArrayList<>();

		
//		int settled = 0;
		for (MatchEvent event : toSettle) {
//			if (settledEventsIds.contains(event.getId())) {
//				continue;
//			}
			
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
				MongoCollection<Document> collection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
				BulkWriteResult bulkWrite = collection.bulkWrite(session, bulkOperations);
				logger.info(Thread.currentThread().getName() + ": " + bulkWrite);
				System.out.println(Thread.currentThread().getName() + ": " + bulkWrite);
			}
		}.execute();
				

		System.out.println(Thread.currentThread().getName() +  " SETTLED PREDICTIONS");
		
		return updated;
	}
	
	
	public FindIterable<Document> getFilteredBets(Bson betsFilter){
		if (betsFilter == null) {
			throw new RuntimeException("Cannot retrieve all bets, please filter");
		}
		
//		Bson pendingBetsFilter =   Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
//		Bson pendingLostBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
//		Bson pendingOrPendingLostFilter = Filters.or(pendingBetsFilter, pendingLostBetsFilter);
		
		
		
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		return betsCollection.find(betsFilter);
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
		
		System.out.println("BET SETTLING " + pendingBets.size());
			
		Map<String, List<WriteModel<Document>>> updatesPerCollection = new HashMap<>();
		updatesPerCollection.put(CollectionNames.BET_PREDICTIONS, new ArrayList<>());
		updatesPerCollection.put(CollectionNames.USER_BETS, new ArrayList<>());
		updatesPerCollection.put(CollectionNames.USERS, new ArrayList<>());
		
		
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
		for (Document betDocument : pendingBets) {
			Map<String, List<WriteModel<Document>>> settleOpenBet = settleOpenBet(betDocument, usersCollection, predictionsCollection, betsCollection);
			
			updatesPerCollection.get(CollectionNames.BET_PREDICTIONS).addAll( settleOpenBet.get(CollectionNames.BET_PREDICTIONS));
			updatesPerCollection.get(CollectionNames.USER_BETS).addAll( settleOpenBet.get(CollectionNames.USER_BETS));
			updatesPerCollection.get(CollectionNames.USERS).addAll( settleOpenBet.get(CollectionNames.USERS));
		
		}

		
		boolean updated = new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {	
				
				if (! updatesPerCollection.get(CollectionNames.BET_PREDICTIONS).isEmpty()) {					
					BulkWriteResult bulkWrite = predictionsCollection.bulkWrite(session, updatesPerCollection.get(CollectionNames.BET_PREDICTIONS));
					logger.info(Thread.currentThread().getName() + " bet pred upd: " + bulkWrite);
				}
				
				if (! updatesPerCollection.get(CollectionNames.USER_BETS).isEmpty()) {					
					BulkWriteResult bulkWrite = betsCollection.bulkWrite(session, updatesPerCollection.get(CollectionNames.USER_BETS));
					logger.info(Thread.currentThread().getName() + " bet upd: " + bulkWrite);
				}
				
				if (! updatesPerCollection.get(CollectionNames.USERS).isEmpty()) {					
					BulkWriteResult bulkWrite = usersCollection.bulkWrite(session, updatesPerCollection.get(CollectionNames.USERS));
					logger.info(Thread.currentThread().getName() + " bet user upd: " + bulkWrite);
				}
				
			}
		}.execute();
				

		System.out.println(Thread.currentThread().getName() +  " SETTLED BETS " + updated);
		
		return updated;
				
		
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
	//@Override
//    private void settleOpenBets() throws Exception {
//    	
//		
//		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
//    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
//    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
//
//		Bson pendingBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
//		Bson pendingLostBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
//		Bson pendingOrPendingLostFilter = Filters.or(pendingBetsFilter, pendingLostBetsFilter);
//		
//		
//		new MongoTransactionalBlock() {
//			int settled = 0;
//
//			@Override
//			public void begin() throws Exception {
//				FindIterable<Document> pendingBets = betsCollection.find(session, pendingOrPendingLostFilter);
//				for (Document betDocument : pendingBets) {
//					/**
//					 * TODO: maybe do this in batches.
//					 */
//					settleOpenBet(session, betDocument, usersCollection, predictionsCollection, betsCollection);
//					++settled;
//				}
//
//				System.out.println("Settle bets: " + settled);
//			}
//		}.execute();
//				
//		
//    }

	@Override
	public void validateUser(String email) {
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				Document userFilter = new Document(MongoFields.EMAIL, email);
				Document validDoc = new Document(MongoFields.VALIDATED, true);
				Document pushDocument = new Document("$set", validDoc);
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
				usersCollection.findOneAndUpdate(session, userFilter, pushDocument);
			}
		}.execute();
	}

	@Override
	public void deleteUser(String mongoId) {
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				Bson userMongoIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoId));
				Bson userBetFilter = Filters.eq(MongoFields.MONGO_USER_ID, mongoId);

				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

				predictionsCollection.deleteMany(userBetFilter);
				betsCollection.deleteMany(userBetFilter);
				usersCollection.deleteOne(userMongoIdFilter);
			}
		}.execute();
	}
	
	 boolean closeMonthlyBalances(ClientSession session) {
		 SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.MONTH_YEAR_DATE_FORMAT);
			String[] currentMonthYear = simpleDateFormat.format(new Date()).split(StringConstants.MINUS);
			int month = Integer.parseInt(currentMonthYear[1]);
			int year = Integer.parseInt(currentMonthYear[0]);
			
			int previousMonth = month == 1 ? 12 : month - 1;
			int previousYear = month == 1 ? year - 1 : year;
		 
		 Bson openBetsOfPreviousMonthFilter = Filters.eq(MongoFields.BET_BELONGING_MONTH, previousMonth);
			Bson openBetsOfPreviousYearFilter = Filters.eq(MongoFields.BET_BELONGING_YEAR, previousYear);
			Bson openBetsOfPreviousMonthYearFilter = Filters.ne(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
			
			Bson combinedFilter = Filters.and(openBetsOfPreviousMonthFilter, openBetsOfPreviousMonthYearFilter, openBetsOfPreviousYearFilter);
			long openBets = fetchFilterSize(CollectionNames.USER_BETS, combinedFilter);
			
			if(openBets > 0) {
				System.out.println("Open bets still in place for " + currentMonthYear);
				return false;
			}
			
			MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
			usersCollection.updateMany(
					session,
	                new Document(),  // No filter, apply to all documents
	             
	                	new Document("$set", new Document(MongoFields.USER_BALANCE_LAST_MONTH, "$balance"))
	                
	            );
			
			usersCollection.updateMany(
					session,
	                new Document(),  // No filter, apply to all documents
	                Updates.set(MongoFields.USER_BALANCE_LAST_MONTH, "$" + MongoFields.USER_BALANCE)  // Set columnB to the value of columnA
	            );
			
			Document validDoc = new Document(MongoFields.USER_BALANCE, ServerConstants.STARTING_BALANCE);
			Document pushDocument = new Document("$set", validDoc);
			
			usersCollection.updateMany(
					session,
	                new Document(), 
	                pushDocument  
	            );
			
			return true;
		
	}
	 
	 boolean commonChecksToCloseMonth(ClientSession session) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.MONTH_YEAR_DATE_FORMAT);
			String[] currentMonthYear = simpleDateFormat.format(new Date()).split(StringConstants.MINUS);
			int month = Integer.parseInt(currentMonthYear[1]);
			int year = Integer.parseInt(currentMonthYear[0]);
			
			int previousMonth = month == 1 ? 12 : month - 1;
			int previousYear = month == 1 ? year - 1 : year;
			
			Bson monthlyWinnerMonthFilter = Filters.eq(MongoFields.AWARD_MONTH, previousMonth);
			Bson monthlyWinnerYearFilter = Filters.eq(MongoFields.AWARD_YEAR, previousYear);
			Bson combined = Filters.and(monthlyWinnerMonthFilter, monthlyWinnerYearFilter);
			
			long existingWinner = fetchFilterSize(CollectionNames.AWARDS, combined);

			if(existingWinner>0) {
				System.out.println("Monthly winner exists");
				return false;
			}
			
			Bson openBetsOfPreviousMonthFilter = Filters.eq(MongoFields.BET_BELONGING_MONTH, previousMonth);
			Bson openBetsOfPreviousYearFilter = Filters.eq(MongoFields.BET_BELONGING_YEAR, previousYear);
			Bson openBetsOfPreviousMonthYearFilter = Filters.ne(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
			
			Bson combinedFilter = Filters.and(openBetsOfPreviousMonthFilter, openBetsOfPreviousMonthYearFilter, openBetsOfPreviousYearFilter);
			long openBets = fetchFilterSize(CollectionNames.USER_BETS, combinedFilter);
			
			if(openBets > 0) {
				System.out.println("Open bets still in place for " + currentMonthYear);
				return false;
			}
		
			
			return true;
	 }
			
	
	/**
	 * 1. All bets of previous month must be settled.
	 * 2. There must not exist a monthly winner record for the previous month.
	 */
	boolean computeMonthWinner(ClientSession session) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SportScoreApiConstants.MONTH_YEAR_DATE_FORMAT);
		String[] currentMonthYear = simpleDateFormat.format(new Date()).split(StringConstants.MINUS);
		int month = Integer.parseInt(currentMonthYear[1]);
		int year = Integer.parseInt(currentMonthYear[0]);
		
		int previousMonth = month == 1 ? 12 : month - 1;
		int previousYear = month == 1 ? year - 1 : year;
		
		
		//at this point all user balances have been reset.
		
		MongoCollection<Document> userCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		
//		Bson kostasg = Filters.eq(MongoFields.EMAIL , "liakos86@gmail.com");
//		FindIterable<Document> find = userCollection.find(kostasg);
//		for (Document document : find) {
//			Collection<Object> values = document.values();
//			System.out.println(values);
//			for (Object value: values) {
//				System.out.println(value.getClass());
//			}
//		}
		
		
	//	List<Bson> pipeline = Arrays.asList(
        Bson max =        Aggregates.group(null, Accumulators.max("maxBalance", "$"+MongoFields.USER_BALANCE_LAST_MONTH));
      //      );

        // Execute the aggregation
        AggregateIterable<Document> result = userCollection.aggregate(Arrays.asList(max));

        

        
				Double maxValue = - 1d;
				
				// Retrieve and print the result
				for (Document doc : result) {
					// Get the maximum value
					maxValue = doc.getDouble("maxBalance");
					System.out.println("Max value: " + maxValue);
				}
				
				Bson matchMaxBalance = Aggregates.match(Filters.gt(MongoFields.USER_BALANCE_LAST_MONTH, maxValue - 0.2));
				List<Bson> asList = Arrays.asList(matchMaxBalance);
				
				 AggregateIterable<Document> aggregate = userCollection.aggregate(asList);
				
				
//				Bson usersMaxBalanceFilter = Filters.eq(MongoFields.USER_BALANCE_LAST_MONTH, maxValue);
				
//				Double tolerance = 1d;
//				Bson lessThanMaxBalanceFilter =Filters.gte(MongoFields.USER_BALANCE_LAST_MONTH, maxValue - tolerance);
//				Bson greaterThanMaxBalanceFilter =Filters.lte(MongoFields.USER_BALANCE_LAST_MONTH, maxValue + tolerance);
				
				// Step 2: Find all records that have the maximum value
				//FindIterable<Document> recordsWithMaxValue = userCollection.find(session, Filters.and(lessThanMaxBalanceFilter, greaterThanMaxBalanceFilter));
				
				List<Document> awardsDocs  = new ArrayList<>();
				for (Document record : aggregate) {
					Document awardDocument = MongoUtils.getAwardDocument(record.getObjectId(MongoFields.MONGO_ID).toString(), maxValue, previousMonth, previousYear);
					System.out.println(record.toJson());
					awardsDocs.add(awardDocument);
				}    
				
				
				MongoCollection<Document> awardsCollection = MongoUtils.getMongoCollection(CollectionNames.AWARDS);

				awardsCollection.insertMany(session, awardsDocs);
				
				return true;	
	}
	
	@Override
	public boolean closeMonthlyBalancesAndComputeMonthWinner() {
		return new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				
				boolean commonChecksToCloseMonth = commonChecksToCloseMonth(session);
				if (!commonChecksToCloseMonth) {
					System.out.println("COMMON CHECKS FOR MONTH FAILED");
					return;
				}
				
				boolean closeMonthlyBalances = closeMonthlyBalances(session);
				if (!closeMonthlyBalances) {
					System.out.println("COULD NOT CLOSE MONTHLY BALANCE");
					return;
				}
				
				computeMonthWinner(session);
				
			}
		}.execute();
		
	}
	
	Map<String, List<WriteModel<Document>>> settleOpenBet( Document betDocument, 
			MongoCollection<Document> usersCollection, MongoCollection<Document> predictionsCollection, MongoCollection<Document> betsCollection) throws Exception {
		
		
		Map<String, List<WriteModel<Document>>> updatesPerCollection = new HashMap<>();
		updatesPerCollection.put(CollectionNames.BET_PREDICTIONS, new ArrayList<>());
		updatesPerCollection.put(CollectionNames.USER_BETS, new ArrayList<>());
		updatesPerCollection.put(CollectionNames.USERS, new ArrayList<>());
		
		String betId = betDocument.getObjectId(MongoFields.MONGO_ID).toString();
		Bson betPredictionsByBetIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, betId);
		FindIterable<Document> betPredictions = predictionsCollection.find(betPredictionsByBetIdFilter);
		
		if (!betPredictions.iterator().hasNext()) {
			System.out.println("******************* NO PREDS ERROR BET");
			return updatesPerCollection;
			
		}
		
		int allPredictions = 0;
		int pendingPredictionsCount = 0;
		int lostPredictionsCount = 0;
		int wonPredictionsCount = 0;
		
		int alreadySettledLostPredictionsCount = 0;
		int alreadySettledWonPredictionsCount = 0;
		
		List<Bson> betUpdateDocuments = new ArrayList<>();//what to update in UserBet collection
		List<Bson> userUpdateDocuments = new ArrayList<>();//what to update in User collection
		List<Bson> predictionsToBeSettledDocuments = new ArrayList<>();//what to update in Predictions collection

		for (Document predictionDocument : betPredictions) {
			allPredictions = allPredictions + 1;
			int predictionStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_STATUS);
			int predictionSettleStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS);
			if (PredictionSettleStatus.SETTLED.getCode() == predictionSettleStatus) {
				if (PredictionStatus.MISSED.getCode() == predictionStatus) {
					alreadySettledLostPredictionsCount = alreadySettledLostPredictionsCount + 1;
				}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
					alreadySettledWonPredictionsCount = alreadySettledWonPredictionsCount + 1;
				}
			}else if (PredictionStatus.PENDING.getCode() == predictionStatus) {
				pendingPredictionsCount = pendingPredictionsCount + 1;
			}else if (PredictionStatus.MISSED.getCode() == predictionStatus) {
				lostPredictionsCount = lostPredictionsCount + 1;
				predictionsToBeSettledDocuments.add(Filters.eq(MongoFields.MONGO_ID, predictionDocument.getObjectId(MongoFields.MONGO_ID)));
			}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
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
					&& wonPredictionsCount > 0 && (wonPredictionsCount+alreadySettledWonPredictionsCount == allPredictions)) {
				Bson updateUserBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, betDocument.getDouble(MongoFields.USER_BET_POSSIBLE_WINNINGS));
				userUpdateDocuments.add(updateUserBalanceDocument);
				
				Bson increaseWonOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_SLIPS, 1);
				Bson increaseWonMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_SLIPS, 1);
				userUpdateDocuments.add(increaseWonMonthlyBetsDocument);
				userUpdateDocuments.add(increaseWonOverallBetsDocument);
				
				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_FAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);
			}else if (lostPredictionsCount > 0 || alreadySettledLostPredictionsCount > 0){

				Bson increaseLostOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_SLIPS, 1);
				Bson increaseLostMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_SLIPS, 1);
				userUpdateDocuments.add(increaseLostMonthlyBetsDocument);
				userUpdateDocuments.add(increaseLostOverallBetsDocument);

				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_UNFAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);
			}else {
				throw new RuntimeException();
			}
		
		}

		Bson increaseWonOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_EVENTS, wonPredictionsCount);
		Bson increaseWonMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_EVENTS, wonPredictionsCount);
		
		userUpdateDocuments.add(increaseWonMonthlyPredictionsDocument);
		userUpdateDocuments.add(increaseWonOverallPredictionsDocument);
		
		
		Bson increaseLostOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_EVENTS, lostPredictionsCount);
		Bson increaseLostMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_EVENTS, lostPredictionsCount);
		
		userUpdateDocuments.add(increaseLostMonthlyPredictionsDocument);
		userUpdateDocuments.add(increaseLostOverallPredictionsDocument);

		Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
		
		
		if (!predictionsToBeSettledDocuments.isEmpty()) {
			Bson settledUpdate = Updates.set(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, PredictionSettleStatus.SETTLED.getCode());
			//predictionsCollection.updateMany(session, Filters.or(predictionsToBeSettledDocuments), settledUpdate);
			
			UpdateManyModel<Document> updateSettledPredictionsDocuments = new UpdateManyModel<>(
					Filters.or(predictionsToBeSettledDocuments),
					settledUpdate
			        );
			
			updatesPerCollection.get(CollectionNames.BET_PREDICTIONS).add(updateSettledPredictionsDocuments);
			
			//bulkOperations.add(updateSettledPredictionsDocuments);
		}
		
		if (!betUpdateDocuments.isEmpty()) {
			System.out.println("SETTLING BET "+ betId + " with " + betUpdateDocuments.size());
			//betsCollection.updateOne(session, betByIdFilter, Updates.combine(betUpdateDocuments));
			
			UpdateOneModel<Document> updatebetDocument = new UpdateOneModel<>(
					betByIdFilter,
					Updates.combine(betUpdateDocuments)
			        );
			
			updatesPerCollection.get(CollectionNames.USER_BETS).add(updatebetDocument);
			
//			bulkOperations.add(updatebetDocument);
		}
		
		if (lostPredictionsCount > 0 || wonPredictionsCount > 0) {
			Bson userIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betDocument.getString(MongoFields.MONGO_USER_ID)));
			//usersCollection.updateOne(session, userIdFilter, Updates.combine(userUpdateDocuments));
			
			UpdateOneModel<Document> updateUserDocument = new UpdateOneModel<>(
					userIdFilter,
					Updates.combine(userUpdateDocuments)
			        );
			
//			bulkOperations.add(updateUserDocument);
			updatesPerCollection.get(CollectionNames.USERS).add(updateUserDocument);
			
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
		finalUser.setPosition(userPosition(finalUser));
		
		finalUser.setMonthlyLostEventsCount(userFromMongo.getInteger(MongoFields.USER_MONTHLY_LOST_EVENTS));
		finalUser.setMonthlyWonEventsCount(userFromMongo.getInteger(MongoFields.USER_MONTHLY_WON_EVENTS));
		finalUser.setMonthlyLostSlipsCount(userFromMongo.getInteger(MongoFields.USER_MONTHLY_LOST_SLIPS));
		finalUser.setMonthlyWonSlipsCount(userFromMongo.getInteger(MongoFields.USER_MONTHLY_WON_SLIPS));

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
	 * @return
	 */
	List<UserBet> getBetsForUser(String userId) {
		Bson userBetsFilter = Filters.eq(MongoFields.MONGO_USER_ID, userId);
		int threeDaysBefore = 10 * 1000 * 24 * 3600;//TODO: change accord
		Bson betMillisFilter = Filters.gt(MongoFields.USER_BET_PLACEMENT_MILLIS, System.currentTimeMillis() - threeDaysBefore);
		Bson userBetsUpToThreeDaysBeforeFilter = Filters.and(userBetsFilter, betMillisFilter);
		
		Executor<UserBet> betsExecutor = new Executor<UserBet>(new TypeToken<UserBet>() {});
		List<UserBet> bets = MongoUtils.get(CollectionNames.USER_BETS, userBetsUpToThreeDaysBeforeFilter, betsExecutor);

		for (UserBet bet : bets) {
			Bson userBetPredictionsFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID,
					bet.getMongoId());
			Executor<UserPrediction> betPredictionsExecutor = new Executor<UserPrediction>(
					new TypeToken<UserPrediction>() {});
			List<UserPrediction> betPredictions = MongoUtils.get(CollectionNames.BET_PREDICTIONS,
					userBetPredictionsFilter, betPredictionsExecutor);
			bet.setPredictions(betPredictions);
			
			betPredictions.forEach(bp -> findTeams(bp));
		}

		return bets;
	}

	void findTeams(UserPrediction p) {
		MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(p.getEventId());
		if (matchEvent != null) {
			p.setHomeTeam(matchEvent.getHome_team());
			p.setAwayTeam(matchEvent.getAway_team());
			return;
		}
		
		Bson homeTeamFilter = Filters.eq(MongoFields.TEAM_ID, p.getHomeTeamId());
		Bson awayTeamFilter = Filters.eq(MongoFields.TEAM_ID, p.getHomeTeamId());
		Bson orFilter = Filters.or(homeTeamFilter, awayTeamFilter);
		Executor<Team> teamExecutor = new Executor<Team>(new TypeToken<Team>() {});
		List<Team> teams = MongoUtils.get(CollectionNames.TEAM, orFilter, teamExecutor);
		if (teams.isEmpty()) {
			Team mockTeam = new Team();
			mockTeam.setName("Missing team");
			mockTeam.setLogo("https://xscore.cc/resb/team/asteras-tripolis.png");
			mockTeam.setId(-1);
			
			p.setHomeTeam(mockTeam);
			p.setAwayTeam(mockTeam);
			return;
		}
		
		for (Team team : teams) {
			if (p.getHomeTeamId() == team.getId()) {
				p.setHomeTeam(team);
			}
			
			if (p.getAwayTeamId() == team.getId()) {
				p.setAwayTeam(team);
			}
		}
	}
	

	List<UserAward> getAwardsForUser(String userId) {
		Bson userAwardsFilter = Filters.eq(MongoFields.FOREIGN_KEY_USER_ID, userId);
		Executor<UserAward> awardsExecutor = new Executor<UserAward>(new TypeToken<UserAward>() {});
		List<UserAward> awards = MongoUtils.get(CollectionNames.EVENTS, userAwardsFilter, awardsExecutor);
		return awards;
	}
	
	long userPosition(User user){
		Bson greaterFromUserBalance = Filters.gt(MongoFields.USER_BALANCE, user.getBalance());
		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		return usersCollection.countDocuments(greaterFromUserBalance) + 1;
	}
	
	UserLevel getUserLevel(User user) {
		return UserLevel.from(user.getOverallWonSlipsCount() + user.getOverallLostSlipsCount(), user.getOverallWonSlipsCount(), 
				user.getOverallWonEventsCount(), user.getBounties().size(), user.getUserAwards().size());
	}

	
//	Set<Integer> getTodaySettledEvents() {
//		MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);
//
//		if (settledBetsCollection.countDocuments() == 0) {
//			return new HashSet<>();
//		}
//		
//		int belongingDay = LocalDate.now().getDayOfMonth();
//		int belongingMonth = LocalDate.now().getMonthValue();//TODO what if some events span to another month?
//		int belongingYear = LocalDate.now().getYear();
//		
//		Bson dayFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_DAY, belongingDay);
//		Bson monthFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_MONTH, belongingMonth);
//		Bson yearFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_YEAR, belongingYear);
//		
//		
//		Set<Integer> settledEventIds = new HashSet<>();
//		FindIterable<Document> settledEventsDocs = settledBetsCollection.find(Filters.and(dayFilter, monthFilter, yearFilter));
//		for (Document settledEventDoc : settledEventsDocs) {
//			settledEventIds.add(settledEventDoc.getInteger(MongoFields.EVENT_ID));
//		}
//		
//		return settledEventIds;
//	}
//	
//	void storeSettledEvent(int eventId) {
//		
//		new MongoTransactionalBlock() {
//			
//			@Override
//			public void begin() throws Exception {
//				MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);
//				Document settledEventDocument = MongoUtils.getSettledEventDocument(eventId);
//				settledBetsCollection.insertOne(settledEventDocument);
//			}
//		}.execute();
//		
//	}
//	
//	void deleteSettledEvent(int eventId) {
//		
//		new MongoTransactionalBlock() {
//			
//			@Override
//			public void begin() throws Exception {
//				MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);
//				Bson settledEventFilter = Filters.eq(MongoFields.EVENT_ID, eventId);
//				settledBetsCollection.deleteOne(session, settledEventFilter);
//			}
//		}.execute();
//		
//	}
	
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

	

	



}
