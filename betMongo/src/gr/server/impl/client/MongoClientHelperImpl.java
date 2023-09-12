package gr.server.impl.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserAward;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
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

		Document existingUserName = new Document(MongoFields.USERNAME, user.getUsername());
		Document existingEmail = new Document(MongoFields.EMAIL, user.getEmail());
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = SyncHelper.getAndOrDocument(filters, MongoFields.OR);

		FindIterable<Document> find = users.find(orDocument);
		Document existingUser = find.first();
		if (existingUser != null) {

			boolean isValid = existingUser.getBoolean(MongoFields.VALIDATED);
			if (isValid) {
				user.setErrorMessage("Username or email already used");
			} else {
				user.setErrorMessage(user.getEmail() + " needs to validate email");
			}

			return user;
//			throw new UserExistsException("User " + user.getUsername() + " or " + user.getEmail() + " already exists");
		}

		Document newUser = SyncHelper.getNewUserDocument(user);
		users.insertOne(newUser);

		User createdUser = new User(newUser.getObjectId(MongoFields.MONGO_ID).toString());
		createdUser.setUsername(newUser.getString(MongoFields.USERNAME));
		createdUser.setEmail(newUser.getString(MongoFields.EMAIL));
		// createdUser.setPassword(newUser.getString(Fields.PASSWORD));
		// createdUser.setPosition(SyncHelper.userPosition(createdUser));//TODO later
		return createdUser;
	}

	@Override
	public User loginUser(User user) {
		MongoClient client = SyncHelper.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);

		Document existingUserName = new Document(MongoFields.USERNAME, user.getUsername());
		Document existingEmail = new Document(MongoFields.EMAIL, user.getEmail());
		List<Document> filters = new ArrayList<>();
		filters.add(existingEmail);
		filters.add(existingUserName);
		Document orDocument = SyncHelper.getAndOrDocument(filters, MongoFields.OR);

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
		MongoClient client = SyncHelper.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB)
				.getCollection(CollectionNames.USERS);
		FindIterable<Document> usersFromMongo = users.find(new Document(MongoFields.MONGO_ID, new ObjectId(mongoId)));

		Document userFromMongo = usersFromMongo.first();

		User user = userFromMongoDocument(userFromMongo);
		user.setUserBets(getBetsForUser(mongoId));
		user.setUserAwards(getAwardsForUser(mongoId));

		return user;
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
		finalUser.setPosition(SyncHelper.userPosition(finalUser));
		return finalUser;
	}

//	@Override
//	public void placeBet(UserBet userBet) {
//		MongoClient client = SyncHelper.getMongoClient();
//		MongoDatabase database = client.getDatabase(CollectionNames.BOUNTY_BET_DB);
//
//		MongoCollection<Document> betsCollection = database.getCollection(CollectionNames.BETS);
//		Document newBet = SyncHelper.getBetDocument(userBet);
//		betsCollection.insertOne(newBet);
//
//		String newBetMongoId = newBet.getObjectId(MongoFields.MONGO_ID).toString();
//
//		MongoCollection<Document> predictionsCollection = database.getCollection(CollectionNames.BET_PREDICTIONS);
//		List<Document> newPredictions = SyncHelper.getPredictionsDocuments(userBet.getPredictions(), newBetMongoId);
//		predictionsCollection.insertMany(newPredictions);
//
//		userBet.setMongoId(newBetMongoId);
//		new SyncHelper().updateUserBalance(userBet);
//	}

	/**
	 * If the input bet is {@link BetStatus#PENDING} we remove the bet amount from
	 * user. If the input bet is {@link BetStatus#SETTLED_FAVOURABLY}we add the bet
	 * earnings to the user.
	 * 
	 * {@link User} will be updated depending on his won/lost {@link UserBet}.
	 * 
	 * @param userBet
	 */
	public static void updateUser(UserBet userBet) {
		MongoCollection<Document> usersCollection = SyncHelper.getMongoCollection(CollectionNames.USERS);
		Document filter = new Document(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoUserId()));

		Document userFieldsDocument = new Document();
		if (userBet.getBetStatus() == BetStatus.SETTLED_FAVOURABLY) {// bet won
			userFieldsDocument.append(MongoFields.USER_BALANCE, userBet.getPossibleEarnings());
		} else if (userBet.getBetStatus() == BetStatus.PENDING) {// bet placed
			userFieldsDocument.append(MongoFields.USER_BALANCE, -1 * (userBet.getBetAmount()));
		} else if (userBet.getBetStatus() == BetStatus.SETTLED_UNFAVOURABLY) {// bet lost
			return; // TODO: Do we need something here?
		}
		Document increaseOrDecreaseDocument = new Document("$inc", userFieldsDocument);
		usersCollection.findOneAndUpdate(filter, increaseOrDecreaseDocument);
	}

	/**
	 * Every bet has a field for the {@link User#getMongoId()} that holds it.
	 * 
	 * @param userId
	 * @return
	 */
	public List<UserBet> getBetsForUser(String userId) {
		Document userBetsFilter = new Document(MongoFields.BET_MONGO_USER_ID, userId);
		Executor<UserBet> betsExecutor = new Executor<UserBet>(new TypeToken<UserBet>() {});
		List<UserBet> bets = SyncHelper.get(CollectionNames.BETS, userBetsFilter, betsExecutor);

		for (UserBet bet : bets) {
			Document userBetPredictionsFilter = new Document(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID,
					bet.getMongoId());
			Executor<UserPrediction> betPredictionsExecutor = new Executor<UserPrediction>(
					new TypeToken<UserPrediction>() {});
			List<UserPrediction> betPredictions = SyncHelper.get(CollectionNames.BET_PREDICTIONS,
					userBetPredictionsFilter, betPredictionsExecutor);
			bet.setPredictions(betPredictions);
		}

		return bets;
	}

	public List<UserAward> getAwardsForUser(String userId) {
		Document userAwardsFilter = new Document(MongoFields.FOREIGN_KEY_USER_ID, userId);
		Executor<UserAward> awardsExecutor = new Executor<UserAward>(new TypeToken<UserAward>() {
		});
		List<UserAward> awards = SyncHelper.get(CollectionNames.EVENTS, userAwardsFilter, awardsExecutor);
		return awards;
	}

	public List<User> retrieveLeaderBoard() {
		Document sortField = new Document(MongoFields.USER_BALANCE, -1);
		Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() {
		});
		List<User> users = SyncHelper.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 20);
		return users;
	}
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

//	@Override
//	public void settleBets(ClientSession session) throws Exception {
//		
//		SyncHelper.settleOpenBets(session);
//		
//
//	}

	/**
	 * Filter the finished events only. Then create filters for predictions related
	 * to this event, but have incorrect prediction type. Then for those related to the
	 * event and have correct prediction type. For now the prediction
	 * category is only {@link PredictionCategory#FINAL_RESULT}.
	 */
	@Override
	public void settlePredictions(ClientSession session, List<MatchEvent> events) throws Exception {

		List<MatchEvent> finishedEvents = events.stream()
				.filter(e -> e.getStatus().equals(MatchEventStatus.FINISHED.getStatusStr()))
				.collect(Collectors.toList());

		for (MatchEvent event : finishedEvents) {
			Integer winningPrediction = event.getWinner_code();
			if (winningPrediction == null || winningPrediction == 0) {
				throw new Exception("No winner code for " + event);
			}

			Bson eventIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_EVENT_ID, event.getId());
			Bson correctPredictionCatFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_CATEGORY,
					PredictionCategory.FINAL_RESULT.getCategoryCode());

			Bson incorrectPredictionTypeFilter = Filters.ne(MongoFields.USER_BET_PREDICTION_TYPE, winningPrediction);
			List<Bson> inCorrectPredictionFilters = new ArrayList<>();
			inCorrectPredictionFilters.add(eventIdFilter);
			inCorrectPredictionFilters.add(incorrectPredictionTypeFilter);
			inCorrectPredictionFilters.add(correctPredictionCatFilter);
			
			Bson successfulStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_STATUS,
					PredictionStatus.MISSED.getCode());
			SyncHelper.updatePredictions(session, Filters.and(inCorrectPredictionFilters),
					successfulStatusDocument);
			
			Bson correctPredictionTypeFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_TYPE, winningPrediction);
			List<Bson> correctPredictionFilters = new ArrayList<>();
			correctPredictionFilters.add(eventIdFilter);
			correctPredictionFilters.add(correctPredictionTypeFilter);
			correctPredictionFilters.add(correctPredictionCatFilter);
			
			Bson unSuccessfulStatusDocument = Updates.set(MongoFields.USER_BET_PREDICTION_STATUS,
					PredictionStatus.CORRECT.getCode());
			SyncHelper.updatePredictions(session, Filters.and(correctPredictionFilters),
					unSuccessfulStatusDocument);
			
		}

	}

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
