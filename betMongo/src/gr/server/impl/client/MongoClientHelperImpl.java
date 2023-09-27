package gr.server.impl.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionSettleStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserAward;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.def.client.MongoClientHelper;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.TransactionalBlock;

public class MongoClientHelperImpl 
implements MongoClientHelper {

	/*
	 * PUBLIC API
	 * 
	 */
	

	@Override
	public User createUser(User user) {
		
		new TransactionalBlock() {
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

		return user;
	}
	
	public void placeBet(UserBet userBet) {
		
		new TransactionalBlock() {
			@Override
			public void begin() throws Exception {
				userBet.setBetStatus(BetStatus.PENDING);
				userBet.setBetPlaceMillis(System.currentTimeMillis());
				
				userBet.setBelongingMonth(LocalDate.now().getMonthValue());//TODO what if some events span to another month?
				userBet.setBelongingYear(LocalDate.now().getYear());

				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
				Document newBet = MongoUtils.getBetDocument(userBet);
				betsCollection.insertOne(newBet);
				
				String newBetMongoId = newBet.getObjectId(MongoFields.MONGO_ID).toString();
				
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
				List<Document> newPredictions = MongoUtils.getPredictionsDocuments(userBet, newBetMongoId);
				predictionsCollection.insertMany(newPredictions);
				
				userBet.setMongoId(newBetMongoId);
				
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
				Bson filter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoUserId()));
				Bson increaseBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, (-1 * userBet.getBetAmount()));
				usersCollection.findOneAndUpdate(session, filter, increaseBalanceDocument);
				
			}
		}.execute();
	}

	public List<User> retrieveLeaderBoard() {
		Document sortField = new Document(MongoFields.USER_BALANCE, -1);
		Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() {});
		List<User> users = MongoUtils.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 20);
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
	public void settlePredictions(ClientSession session, Set<MatchEvent> finishedEvents) throws Exception {

		Set<Integer> settledEventsIds = getTodaySettledEvents();
		
		for (MatchEvent event : finishedEvents) {
			if (settledEventsIds.contains(event.getId())) {
				continue;
			}
			
			Integer winningPrediction = event.getWinner_code();
			if (winningPrediction == null || winningPrediction == 0) {
				continue;
				//throw new Exception("No winner code for " + event);
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
			
			MongoCollection<Document> collection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);
			collection.updateMany(session, Filters.and(inCorrectPredictionFilters), unSuccessfulStatusDocument);
			collection.updateMany(session, Filters.and(correctPredictionFilters), successfulStatusDocument);
		
			storeSettledEvent(event.getId());
		
		}

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
    public void settleOpenBets(ClientSession session) throws Exception {
    	MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		Bson pendingBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
		Bson pendingLostBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
		Bson pendingOrPendingLostFilter = Filters.or(pendingBetsFilter, pendingLostBetsFilter);
		FindIterable<Document> pendingBets = betsCollection.find(session, pendingOrPendingLostFilter);
		
		for (Document betDocument : pendingBets) {
			
			settleOpenBet(session, betDocument, usersCollection, predictionsCollection, betsCollection);
			
			
			
		}
    }

	void settleOpenBet(ClientSession session, Document betDocument, MongoCollection<Document> usersCollection, MongoCollection<Document> predictionsCollection, MongoCollection<Document> betsCollection) throws Exception {
		String betId = betDocument.getObjectId(MongoFields.MONGO_ID).toString();
		Bson betPredictionsByBetIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, betId);
		FindIterable<Document> betPredictions = predictionsCollection.find(session, betPredictionsByBetIdFilter);
		
		if (!betPredictions.iterator().hasNext()) {
			throw new Exception("Bet without predictions: " + betId);
		}
		
		int pendingPredictionsCount = 0;
		int lostPredictionsCount = 0;
		int wonPredictionsCount = 0;
		
		List<Bson> betUpdateDocuments = new ArrayList<>();//what to update in UserBet collection
		List<Bson> userUpdateDocuments = new ArrayList<>();//what to update in User collection
		List<Bson> predictionsUpdateDocuments = new ArrayList<>();//what to update in Predictions collection

		for (Document predictionDocument : betPredictions) {
			int predictionStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_STATUS);
			int predictionSettleStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS);
			if (PredictionSettleStatus.SETTLED.getCode() == predictionSettleStatus) {
				//nothing
				continue;
			}else if (PredictionStatus.PENDING.getCode() == predictionStatus) {
				pendingPredictionsCount = pendingPredictionsCount + 1;
			}else if (PredictionStatus.MISSED.getCode() == predictionStatus) {
				lostPredictionsCount = lostPredictionsCount + 1;
				predictionsUpdateDocuments.add(Filters.eq(MongoFields.MONGO_ID, predictionDocument.getObjectId(MongoFields.MONGO_ID)));
			}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
				wonPredictionsCount = wonPredictionsCount + 1;
				predictionsUpdateDocuments.add(Filters.eq(MongoFields.MONGO_ID, predictionDocument.getObjectId(MongoFields.MONGO_ID)));
			}
		}
		
		
		BetStatus betStatus = BetStatus.fromCode(betDocument.getInteger(MongoFields.BET_STATUS, 0));
		if (lostPredictionsCount > 0 && pendingPredictionsCount > 0 && BetStatus.PENDING.equals(betStatus)) {
			Bson setLostFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.PENDING_LOST.getCode());
			betUpdateDocuments.add(setLostFilter);
		}else if (pendingPredictionsCount == 0) {
			
			if (lostPredictionsCount == 0 && wonPredictionsCount > 0) {
				Bson updateUserBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, betDocument.getDouble(MongoFields.USER_BET_POSSIBLE_WINNINGS));
				userUpdateDocuments.add(updateUserBalanceDocument);
				
				Bson increaseWonOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_SLIPS, 1);
				Bson increaseWonMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_SLIPS, 1);
				userUpdateDocuments.add(increaseWonMonthlyBetsDocument);
				userUpdateDocuments.add(increaseWonOverallBetsDocument);
				
				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_FAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);
			}else if (lostPredictionsCount > 0) {
				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_UNFAVOURABLY.getCode());
				betUpdateDocuments.add(setWonFilter);

				Bson increaseLostOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_SLIPS, 1);
				Bson increaseLostMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_SLIPS, 1);
				userUpdateDocuments.add(increaseLostMonthlyBetsDocument);
				userUpdateDocuments.add(increaseLostOverallBetsDocument);
			}
			
			Bson increaseWonOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_EVENTS, wonPredictionsCount);
			Bson increaseWonMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_EVENTS, wonPredictionsCount);
			
			userUpdateDocuments.add(increaseWonMonthlyPredictionsDocument);
			userUpdateDocuments.add(increaseWonOverallPredictionsDocument);


			Bson increaseLostOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_EVENTS, lostPredictionsCount);
			Bson increaseLostMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_EVENTS, lostPredictionsCount);
			
		
			userUpdateDocuments.add(increaseLostMonthlyPredictionsDocument);
			userUpdateDocuments.add(increaseLostOverallPredictionsDocument);
		
		}

		Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
		
		System.out.println("SETTLING BET "+ betId + " with " + betUpdateDocuments.size());
		
		betsCollection.updateOne(session, betByIdFilter, Updates.combine(betUpdateDocuments));
		
		Bson userIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betDocument.getString(MongoFields.BET_MONGO_USER_ID)));
		usersCollection.updateOne(session, userIdFilter, Updates.combine(userUpdateDocuments));

		if (!predictionsUpdateDocuments.isEmpty()) {
			Bson settledUpdate = Updates.set(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, PredictionSettleStatus.SETTLED.getCode());
			predictionsCollection.updateMany(session, Filters.or(predictionsUpdateDocuments), settledUpdate);
		}
		
	}

	@Override
	public void validateUser(String email) {
		new TransactionalBlock() {
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
		new TransactionalBlock() {
			@Override
			public void begin() throws Exception {
				Bson userMongoIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoId));
				Bson userBetFilter = Filters.eq(MongoFields.BET_MONGO_USER_ID, mongoId);

				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
				MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

				predictionsCollection.deleteMany(userBetFilter);
				betsCollection.deleteMany(userBetFilter);
				usersCollection.deleteOne(userMongoIdFilter);
			}
		}.execute();
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
		Bson userBetsFilter = Filters.eq(MongoFields.BET_MONGO_USER_ID, userId);
		int threeDaysBefore = 1 * 1000 * 24 * 3600;//TODO: change accord
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
		}

		return bets;
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
	
	Set<Integer> getTodaySettledEvents() {
		MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);

		if (settledBetsCollection.countDocuments() == 0) {
			return new HashSet<>();
		}
		
		int belongingDay = LocalDate.now().getDayOfMonth();
		int belongingMonth = LocalDate.now().getMonthValue();//TODO what if some events span to another month?
		int belongingYear = LocalDate.now().getYear();
		
		Bson dayFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_DAY, belongingDay);
		Bson monthFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_MONTH, belongingMonth);
		Bson yearFilter = Filters.eq(MongoFields.SETTLED_EVENT_BELONGING_YEAR, belongingYear);
		
		
		Set<Integer> settledEventIds = new HashSet<>();
		FindIterable<Document> settledEventsDocs = settledBetsCollection.find(Filters.and(dayFilter, monthFilter, yearFilter));
		for (Document settledEventDoc : settledEventsDocs) {
			settledEventIds.add(settledEventDoc.getInteger(MongoFields.EVENT_ID));
		}
		
		return settledEventIds;
	}
	
	void storeSettledEvent(int eventId) {
		
		new TransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);
				Document settledEventDocument = MongoUtils.getSettledEventDocument(eventId);
				settledBetsCollection.insertOne(settledEventDocument);
			}
		}.execute();
		
	}
	
	void deleteSettledEvent(int eventId) {
		
		new TransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				MongoCollection<Document> settledBetsCollection = MongoUtils.getMongoCollection(CollectionNames.SETTLED_EVENTS);
				Bson settledEventFilter = Filters.eq(MongoFields.EVENT_ID, eventId);
				settledBetsCollection.deleteOne(session, settledEventFilter);
			}
		}.execute();
		
	}

}
