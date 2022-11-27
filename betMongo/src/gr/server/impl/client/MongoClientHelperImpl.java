package gr.server.impl.client;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.Fields;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserAward;
import gr.server.data.user.model.objects.UserBet;
import gr.server.def.client.MongoClientHelper;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.SyncHelper;
import gr.server.transaction.helper.TransactionalBlock;

public class MongoClientHelperImpl implements MongoClientHelper {

	/*
	 * PUBLIC API
	 * 
	 */

	@Override
	public User createUser(User user) {
		MongoClient client = SyncHelper.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);
		
		Document existingUserName =  new Document(Fields.USERNAME, user.getUsername());
		Document existingEmail = new Document(Fields.EMAIL, user.getEmail());
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = SyncHelper.getOrDocument(filters);
		
		FindIterable<Document> find = users.find(orDocument);
		Document existingUser = find.first();
		if (existingUser != null) {
			
			boolean isValid = existingUser.getBoolean(Fields.VALIDATED);
			if (isValid) {
				user.setErrorMessage("Username or email already used");
			}else {
				user.setErrorMessage( user.getEmail() + " needs to validate email");
			}
			
			return user;
//			throw new UserExistsException("User " + user.getUsername() + " or " + user.getEmail() + " already exists");
		}
		
		Document newUser = SyncHelper.getNewUserDocument(user);
		users.insertOne(newUser);

		User createdUser = new User(newUser.getObjectId(Fields.MONGO_ID).toString());
		createdUser.setUsername(newUser.getString(Fields.USERNAME));
		createdUser.setEmail(newUser.getString(Fields.EMAIL));
		//createdUser.setPassword(newUser.getString(Fields.PASSWORD));
		//createdUser.setPosition(SyncHelper.userPosition(createdUser));//TODO later
		return createdUser;
	}
	
	@Override
	public User loginUser(User user) {
		MongoClient client = SyncHelper.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);
		
		Document existingUserName =  new Document(Fields.USERNAME, user.getUsername());
		Document existingEmail = new Document(Fields.EMAIL, user.getEmail());
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = SyncHelper.getOrDocument(filters);
		
		FindIterable<Document> find = users.find(orDocument);
		Document existingUser = find.first();
		if (existingUser != null) {
			
			boolean isValid = existingUser.getBoolean(Fields.VALIDATED);
			if (isValid) {
				user.setErrorMessage("Username or email already used");
			}else {
				user.setErrorMessage( user.getEmail() + " needs to validate email");
			}
			
			return user;
//			throw new UserExistsException("User " + user.getUsername() + " or " + user.getEmail() + " already exists");
		}
		
		Document newUser = SyncHelper.getNewUserDocument(user);
		users.insertOne(newUser);

		User createdUser = new User(newUser.getObjectId(Fields.MONGO_ID).toString());
		createdUser.setUsername(newUser.getString(Fields.USERNAME));
		createdUser.setEmail(newUser.getString(Fields.EMAIL));
		//createdUser.setPassword(newUser.getString(Fields.PASSWORD));
		//createdUser.setPosition(SyncHelper.userPosition(createdUser));//TODO later
		return createdUser;
	}


	@Override
	public User getUser(String mongoId) {
		MongoClient client = SyncHelper.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);
		FindIterable<Document> usersFromMongo = users.find(new Document(Fields.MONGO_ID, new ObjectId(mongoId)));

		if (!usersFromMongo.iterator().hasNext()) {
			return null;
		}

		Document userFromMongo = usersFromMongo.iterator().next();
		String userString = userFromMongo.toJson();
		User finalUser = new Gson().fromJson(userString, new TypeToken<User>() {
		}.getType());
		finalUser.setUserBets(getBetsForUser(mongoId));
		finalUser.setUserAwards(getAwardsForUser(mongoId));
		finalUser.setMongoId(userFromMongo.getObjectId(Fields.MONGO_ID).toString());
		finalUser.setPosition(SyncHelper.userPosition(finalUser));
		return finalUser;
	}
//
//	/**
//	 * We receive a list of leagues.
//	 * we store the leagues without their events.
//	 * We also store the events separately.
//	 */
//	@Override
//	public void storeLeagues(ClientSession session, List<League> leagues) throws ParseException {
//		List<Document> newLeagues = new ArrayList<Document>();
//		for (League league : leagues) {
//			Document newLeague = SyncHelper.getLeagueDocument(league);
//			newLeagues.add(newLeague);
//			storeEvents(session, league.getEvents());
//		}
//
//		MongoClient client = SyncHelper.getMongoClient();
//		MongoCollection<Document> leaguesCollection = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
//				.getCollection(CollectionNames.LEAGUES);
//		
//		leaguesCollection.deleteMany(session, new Document());
//		
//		leaguesCollection.insertMany(session, newLeagues);
//	}
//
//	@Override
//	public List<League> getMongoLeagues() {
//		Executor<League> executor = new Executor<League>(new TypeToken<League>() {
//		});
//		List<League> leagues = SyncHelper.get(CollectionNames.LEAGUES, new Document(), executor);
//		for (League league : leagues) {
//			Document eventsFilter = new Document("league_id", league.getLeagueId()).append("country_id",
//					league.getCountryId());
//			Executor<Event> eventsExecutor = new Executor<Event>(new TypeToken<Event>() {
//			});
//			List<Event> leagueEvents = SyncHelper.get(CollectionNames.EVENTS, eventsFilter, eventsExecutor);
//			league.setEvents(leagueEvents);
//		}
//
//		return leagues;
//	}
//
	@Override
	public UserBet placeBet(UserBet userBet) {
		MongoClient client = SyncHelper.getMongoClient();
		MongoDatabase database = client.getDatabase(CollectionNames.BOUNTY_BET_DB);
		MongoCollection<Document> betsCollection = database.getCollection(CollectionNames.BETS);
		Document newBet = SyncHelper.getBetDocument(userBet);
		betsCollection.insertOne(newBet);
		userBet.setMongoId(newBet.getObjectId(Fields.MONGO_ID).toString());
		//SyncHelper.updateUser(userBet);
		return userBet;
	}
//
//	@Override
//	public void settleBets(ClientSession session, List<SettledEvent> settled) {
//		List<Document> possibleValues = Arrays
//				.asList(new Document[] { new Document("betStatus", BetStatus.PENDING.getCode()),
//						new Document("betStatus", BetStatus.PENDING_LOST.getCode()) });
//		Document openPendingBetsFilter = SyncHelper.getOrDocument("betStatus", possibleValues);
//
//		Executor<UserBet> executor = new Executor<UserBet>(new TypeToken<UserBet>() {
//		});
//		List<UserBet> openBets = SyncHelper.get(CollectionNames.BETS, openPendingBetsFilter, executor);
//
////		Document eventFilter = new Document("match_status", "FT");
////		Executor<Event> executorEvent = new Executor<Event>(new TypeToken<Event>() {
////		});
////		List<Event> finished = SyncHelper.get(CollectionNames.EVENTS, eventFilter, executorEvent);
////
////		Map<String, Event> finishedEvents = new HashMap<String, Event>();
////		for (Event event : finished) {
////			finishedEvents.put(event.getMatchId(), event);
////		}
//
//		for (UserBet userBet : openBets) {
//			settleBet(userBet, settled);
//		}
//	}
//
//	// END OF PUBLIC API
//
////	//TODO this is totally wrong
////	void settleBet(UserBet userBet, Map<String, Event> finishedEvents) {
////		for (UserPrediction prediction : userBet.getPredictions()) {
////			if (PredictionStatus.PENDING != prediction.getPredictionStatus()) {
////				continue;
////			}
////
////			Event predictionEvent = finishedEvents.get(prediction.getEventId());
////			PredictionType result = calculateEventResult(predictionEvent);
////			if (result == prediction.getPredictionType()) {
////				prediction.setPredictionStatus(PredictionStatus.CORRECT);
////			} else {
////				prediction.setPredictionStatus(PredictionStatus.MISSED);
////				userBet.setBetStatus(BetStatus.PENDING_LOST);
////			}
////		}
////
////		if (shouldSettleFavourably(userBet)) {
////			settleFavourably(userBet);
////		}
////
////		if (shouldSettleUnfavourably(userBet)) {
////			settleUnfavourably(userBet);
////		}
////
////		userBet = SyncHelper.updateBet(userBet);
////		SyncHelper.updateUser(userBet);
////	}
//	
//	/**
//	 * A {@link UserBet} contains a list of {@link UserPrediction}s.
//	 * If one is {@link PredictionStatus#MISSED} the whole bet is lost.
//	 * Initially it is {@link BetStatus#PENDING_LOST}, until all predictions are settled.
//	 * 
//	 * @param userBet
//	 * @param settledEvents
//	 */
//	void settleBet(UserBet userBet, List<SettledEvent> settledEvents) {
//		for (UserPrediction prediction : userBet.getPredictions()) {
//			if (PredictionStatus.PENDING != prediction.getPredictionStatus()) {
//				continue;
//			}
//
//			Set<SettledEvent> possibleSettledEvents = settledEvents.stream().filter(e -> e.getEventId().equals(prediction.getEventId())).collect(Collectors.toSet());
//			if (possibleSettledEvents == null || possibleSettledEvents.isEmpty()) {
//				continue;
//			}
//			
//			SettledEvent settledEvent = possibleSettledEvents.iterator().next(); 
//			if (settledEvent.getSuccessfulPredictions().contains(prediction.getPredictionType())) {
//				prediction.setPredictionStatus(PredictionStatus.CORRECT);
//			} else {
//				prediction.setPredictionStatus(PredictionStatus.MISSED);
//				userBet.setBetStatus(BetStatus.PENDING_LOST);
//			}
//		}
//
//		if (shouldSettleFavourably(userBet)) {
//			settleFavourably(userBet);
//		}
//
//		if (shouldSettleUnfavourably(userBet)) {
//			settleUnfavourably(userBet);
//		}
//
//		userBet = SyncHelper.updateBet(userBet);
//		SyncHelper.updateUser(userBet);
//	}
//
//	void settleUnfavourably(UserBet userBet) {
//		userBet.setBetStatus(BetStatus.SETTLED_UNFAVOURABLY);
//	}
//
//	void settleFavourably(UserBet userBet) {
//		userBet.setBetStatus(BetStatus.SETTLED_FAVOURABLY);
//	}
//
//	PredictionType calculateEventResult(Event predictionEvent) {
//		Integer matchHometeamScore = Integer.parseInt(predictionEvent.getMatchHometeamScore());
//		Integer matchAwayTeamScore = Integer.parseInt(predictionEvent.getMatchAwayteamScore());
//		PredictionType predictionEventResult = matchHometeamScore > matchAwayTeamScore ? PredictionType.HOME_WIN
//				: (matchHometeamScore.equals(matchAwayTeamScore) ? PredictionType.DRAW : PredictionType.AWAY_WIN);
//		return predictionEventResult;
//	}
//
//	/**
//	 * If user missed one prediction, the {@link UserBet} is considered lost.
//	 * 
//	 */
//	boolean shouldSettleUnfavourably(UserBet userBet) {
//		boolean atLeastOneMissed = false;
//		for (UserPrediction prediction : userBet.getPredictions()) {
//			if (PredictionStatus.PENDING == prediction.getPredictionStatus()) {
//				return false;
//			}
//			if (PredictionStatus.MISSED == prediction.getPredictionStatus()) {
//				atLeastOneMissed = true;
//			}
//		}
//
//		if (atLeastOneMissed) {//no pending and at least one missed.
//			return true;
//		}
//		return false;
//	}
//
//	boolean shouldSettleFavourably(UserBet userBet) {
//		for (UserPrediction prediction : userBet.getPredictions()) {
//			if (PredictionStatus.CORRECT != prediction.getPredictionStatus()) {
//				return false;
//			}
//		}
//		return true;
//	}
//
	/**
	 * Every bet has a field for the {@link User#getMongoId()} that holds it.
	 * 
	 * @param userId
	 * @return
	 */
	public List<UserBet> getBetsForUser(String userId) {
		Document userBetsFilter = new Document(Fields.BET_MONGO_USER_ID, userId);
		Executor<UserBet> betsExecutor = new Executor<UserBet>(new TypeToken<UserBet>() {
		});
		List<UserBet> bets = SyncHelper.get(CollectionNames.BETS, userBetsFilter, betsExecutor);
		return bets;
	}

	public List<UserAward> getAwardsForUser(String userId) {
		Document userAwardsFilter = new Document(Fields.FOREIGN_KEY_USER_ID, userId);
		Executor<UserAward> awardsExecutor = new Executor<UserAward>(new TypeToken<UserAward>() {
		});
		List<UserAward> awards = SyncHelper.get(CollectionNames.EVENTS, userAwardsFilter, awardsExecutor);
		return awards;
	}

	public List<User> retrieveLeaderBoard() {
		Document sortField = new Document(Fields.USER_BALANCE, -1);
		Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() {
		});
		List<User> users = SyncHelper.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 20);
		return users;
	}
//
//	/**
//	 * Every event is first deleted and then alltogether inserted into the events
//	 * collection. In case event is not existent nothing is deleted.
//	 * 
//	 * @param session
//	 * @param events
//	 * @throws ParseException
//	 */
//	public void storeEvents(ClientSession session, List<Event> events) throws ParseException {
//
//		if (events.isEmpty()) {
//			return;
//		}
//
//		MongoClient client = SyncHelper.getMongoClient();
//		MongoCollection<Document> eventsCollection = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
//				.getCollection(CollectionNames.EVENTS);
//
//		List<Document> newEvents = new ArrayList<Document>();
//		for (Event event : events) {
//			Document match_by_id = new Document(Fields.MATCH_ID, event.getMatchId());
//			eventsCollection.deleteOne(session, match_by_id);
//
//			Document newEvent = SyncHelper.getEventDocument(event);
//			newEvents.add(newEvent);
//		}
//
//		eventsCollection.insertMany(session, newEvents);
//	}
//
//	/**
//	 * Runs on the midnight of the first day of the month. Finds the winner. Stores
//	 * an award. Updates the winner's fields. Restores every user's balance to
//	 * {@link ApiFootBallConstants#STARTING_BALANCE}. Deletes bets going 2 months
//	 * ago.
//	 * 
//	 * @throws Exception
//	 * 
//	 * @see TimerTaskHelper#getMonthChangeCheckerTask()
//	 */
//	public void settleMonthlyAward(String monthToSettle) throws Exception {
//
//		new TransactionalBlock() {
//			@Override
//			public void begin() {
//				Document sortField = new Document(Fields.USER_BALANCE, -1);
//				Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() {
//				});
//				List<User> monthWinners = SyncHelper.getSorted(CollectionNames.USERS, usersExecutor, new Document(),
//						sortField, 1);
//				// TODO tied users
//				Document awardDocument = SyncHelper.createAwardFor(session, monthWinners.get(0));
//				SyncHelper.updateUserAwards(session, monthWinners.get(0), awardDocument.getObjectId(Fields.MONGO_ID));
//
//				SyncHelper.restoreUserBalance(session);
//				SyncHelper.deleteUserBetsFor(session, DateUtils.getPastMonthAsString(2));
//
//			}
//		}.execute();
//	}
//
//	/**
//	 * Delete X days ago based on event start time.
//	 */
//	public void deletePastEvents() {
//		Document lessThanDate = new Document("$lt", DateUtils.getStaleEventsDate().getTime());
//		Document staleEventsFilter = new Document(Fields.MATCH_FULL_DATE, lessThanDate);
//		SyncHelper.deleteMany(CollectionNames.EVENTS, staleEventsFilter);
//	}
//
//	public void deleteBountiesUntil(ClientSession session, Date bountiesExpirationDate) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void storeSettledEvents(ClientSession session, List<SettledEvent> settledEvents) {
//		List<Document> documents = new ArrayList<>();
//		settledEvents.forEach(settledEvent -> 
//		documents.add(SyncHelper.createSettledDocument(settledEvent))
//		);
//		MongoClient client = SyncHelper.getMongoClient();
//		MongoCollection<Document> settledEventsCollection = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
//				.getCollection(CollectionNames.SETTLED_EVENTS);
//		settledEventsCollection.deleteMany(session, new Document());
//		settledEventsCollection.insertMany(session, documents);
//		// TODO Auto-generated method stub
//		
//	}
//

	
	@Override
	public void validateUser(String email) {
		new TransactionalBlock() {
			@Override
			public void begin() throws Exception {
				SyncHelper.validateUser(session, email);
			}
		}.execute();
	}
}
