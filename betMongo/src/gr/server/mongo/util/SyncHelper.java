package gr.server.mongo.util;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.ConnectionString;
//import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.transaction.helper.TransactionalBlock;

/**
 * 
 * This class performs Mongo related actions on a cloud replica set instance.
 * For local mongodb actions @see {@link MongoCollectionUtils} 
 *
 * TODO: if we launch a cloud mongo db, we need to limit the connect ONLY from our server's IP address.
 * Atlas -> Security -> Database access
 * 
 * @author liako
 *
 */
public class SyncHelper {
	
	//final static String conn = "mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority";

	static MongoClient MONGO_CLIENT;

    public static <E> ArrayList<E> get(String collectionString, Document filter, Executor<E> e){
		MongoCollection<Document> collection = getMongoCollection(collectionString);
		FindIterable<Document> find = collection.find(filter);
		ArrayList<E> list  = new ArrayList<E>();
		for (Document document : find) {
			 String json = document.toJson();
			 E object = (E) e.execute(json);
			 if (object instanceof UserBet){
				 ((UserBet)object).setMongoId(document.getObjectId(MongoFields.MONGO_ID).toString());
			 }
			 
			list.add(object);
		}
		return list;
	}
    
    public static <E> ArrayList<E> getSorted(String collectionString,  Executor<E> e, Document findFilter, Document sortFilter, int limit){
		 MongoCollection<Document> collection = getMongoCollection(collectionString);
		 FindIterable<Document> find = collection.find(findFilter).limit(limit).sort(sortFilter);
		 ArrayList<E> list  = new ArrayList<E>();
		 for (Document document : find) {
			 String json = document.toJson();
			 E object = (E) e.execute(json);
			 if (object instanceof User){//very very dirty
				((User) object).setMongoId(document.getObjectId(MongoFields.MONGO_ID).toString()); 
				((User) object).setUserBets(new ArrayList<>());
			 }
			list.add(object);
		 }
		 return list;
	}
    
	public static void placeBet(UserBet userBet) {
		
		new TransactionalBlock() {
			@Override
			public void begin() throws Exception {
				userBet.setBetStatus(BetStatus.PENDING);

				MongoClient client = getMongoClient();
				MongoDatabase database = client.getDatabase(CollectionNames.BOUNTY_BET_DB);
				
				MongoCollection<Document> betsCollection = database.getCollection(CollectionNames.BETS);
				Document newBet = getBetDocument(userBet);
				betsCollection.insertOne(newBet);
				
				String newBetMongoId = newBet.getObjectId(MongoFields.MONGO_ID).toString();
				
				MongoCollection<Document> predictionsCollection = database.getCollection(CollectionNames.BET_PREDICTIONS);
				List<Document> newPredictions = SyncHelper.getPredictionsDocuments(userBet.getPredictions(), newBetMongoId);
				predictionsCollection.insertMany(newPredictions);
				
				userBet.setMongoId(newBetMongoId);
				updateUserBalance(session, userBet.getMongoUserId(), -1 * userBet.getPossibleEarnings());
			}
		}.execute();
	}
    
    public static void settleOpenBets(ClientSession session) throws Exception {
    	MongoCollection<Document> betsCollection = getMongoCollection(CollectionNames.BETS);
    	MongoCollection<Document> predictionsCollection = getMongoCollection(CollectionNames.BET_PREDICTIONS);

		Bson pendingBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
		FindIterable<Document> pendingBets = betsCollection.find(session, pendingBetsFilter);
		
		for (Document betDocument : pendingBets) {
			String betId = betDocument.getObjectId(MongoFields.MONGO_ID).toString();
			Bson betPredictionsByBetIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, betId);
			FindIterable<Document> betPredictions = predictionsCollection.find(session, betPredictionsByBetIdFilter);
			
			if (!betPredictions.iterator().hasNext()) {
				throw new Exception("Bet without predictions: " + betId);
			}
			
			boolean pendingBetPrediction = false;
			boolean lostBetPrediction = false;
			for (Document predictionDocument : betPredictions) {
				int predictionStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_STATUS);
				if (PredictionStatus.PENDING.getCode() == predictionStatus) {
					pendingBetPrediction = true;
					continue;
				}
				
				if (PredictionStatus.MISSED.getCode() == predictionStatus) {
					Bson setLostFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_UNFAVOURABLY.getCode());
					Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
					betsCollection.updateOne(session, betByIdFilter, setLostFilter);
					lostBetPrediction = true;
					break;
				}
			}
			
			if(!pendingBetPrediction && !lostBetPrediction) {
				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_FAVOURABLY.getCode());
				Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
				betsCollection.updateOne(session, betByIdFilter, setWonFilter);
				updateUserBalance(session, betDocument.getString(MongoFields.BET_MONGO_USER_ID), betDocument.getDouble(MongoFields.USER_BET_POSSIBLE_WINNINGS));
			}
		}
    }

    public static void updatePredictions(ClientSession session, Bson eventAndPredictionTypeDocument, Bson successfulStatusDocument) {
    	MongoCollection<Document> collection = getMongoCollection(CollectionNames.BET_PREDICTIONS);
    	collection.updateMany(session, eventAndPredictionTypeDocument, successfulStatusDocument);
    }

	public static List<Document> getPredictionsDocuments(List<UserPrediction> predictions, String newBetMongoId) {
		List<Document> newBetPredictions = new ArrayList<>();

		for (UserPrediction prediction : predictions) {
			Document newBetPrediction = new Document(MongoFields.USER_BET_PREDICTION_EVENT_ID, prediction.getEventId())
					.append(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, newBetMongoId)
					.append(MongoFields.USER_BET_PREDICTION_TYPE, prediction.getPredictionType().getCode())
					.append(MongoFields.USER_BET_PREDICTION_CATEGORY, prediction.getPredictionCategory().getCategoryCode())
					.append(MongoFields.USER_BET_PREDICTION_STATUS, PredictionStatus.PENDING.getCode())
					.append(MongoFields.USER_BET_PREDICTION_ODD_VALUE, prediction.getOddValue()

					);
			newBetPredictions.add(newBetPrediction);
		}
		
		return newBetPredictions;
	}

	public static Document getBetDocument(UserBet userBet) {
		Document newBet = new Document(MongoFields.BET_MONGO_USER_ID, userBet.getMongoUserId())
				.append(MongoFields.BET_AMOUNT, userBet.getBetAmount())
				.append(MongoFields.BET_STATUS, userBet.getBetStatus().getCode())
				.append(MongoFields.BET_PLACE_DATE, userBet.getBetPlaceDate())
				.append(MongoFields.BET_BELONGING_MONTH, DateUtils.getPastMonthAsString(0));

		return newBet;
	}

	public static Document getNewUserDocument(User user) {
		 return new Document(MongoFields.USERNAME, user.getUsername())
				 .append(MongoFields.EMAIL, user.getEmail())
				 .append(MongoFields.PASSWORD, user.getPassword())
				 .append(MongoFields.VALIDATED, false)
		 .append(MongoFields.USER_BALANCE, SportScoreApiConstants.STARTING_BALANCE)
		 .append(MongoFields.USER_AWARDS, new BasicDBList());
	}

	public static Document getAndOrDocument(
			List<Document> possibleValues, String andOr) {
		BasicDBList orList = new BasicDBList();
		for (Document document : possibleValues){
			orList.add(document);
		}
		return new Document(andOr, orList);
	}
	
	public static long userPosition(User user){
		Document userBalance = new Document("$gt", user.getBalance());
		Document greaterBalancedUsers = new Document(MongoFields.USER_BALANCE, userBalance);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		return usersCollection.countDocuments(greaterBalancedUsers);
	}
	
	/**
	 * If the input bet is {@link BetStatus#PENDING} we remove the bet amount from
	 * user. If the input bet is {@link BetStatus#SETTLED_FAVOURABLY}we add the bet
	 * earnings to the user.
	 * 
	 * {@link User} will be updated depending on his won/lost {@link UserBet}.
	 * 
	 * @param mongoUserId
	 */
	public static void updateUserBalance(ClientSession session, String mongoUserId, Double amount) {
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		Bson filter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoUserId));
		Bson increaseBalanceDocument = Updates.inc(MongoFields.USER_BALANCE, amount);
		usersCollection.findOneAndUpdate(session, filter, increaseBalanceDocument);
	}

	public static void updateUserAwards(ClientSession startSession, User monthWinner, ObjectId awardId) {
		Document userFilter = new Document(MongoFields.FOREIGN_KEY_USER_ID, monthWinner.getMongoId());
		Document newAwardDocument = new Document(MongoFields.USER_AWARDS_IDS, awardId.toString());
		Document pushDocument = new Document("$push", newAwardDocument);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		usersCollection.findOneAndUpdate(startSession, userFilter, pushDocument);
	}

	public static void restoreUserBalance(ClientSession startSession) {
		Document balanceFilter = new Document(MongoFields.USER_BALANCE, SportScoreApiConstants.STARTING_BALANCE);
		Document setBalance = new Document("$set", balanceFilter);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		usersCollection.updateMany(startSession, new Document(), setBalance);
	}

	public static void deleteUserBetsFor(ClientSession startSession, String pastMonthAsString) {
		Document belongingMonthFilter = new Document(MongoFields.BET_BELONGING_MONTH, pastMonthAsString);
		MongoCollection<Document> betsCollection = getMongoCollection(CollectionNames.BETS);
		betsCollection.deleteMany(startSession, belongingMonthFilter);
	}
	
	public static void deleteMany(String collectionName, Document deleteFilter) {
		MongoCollection<Document> collection = getMongoCollection(collectionName);
		collection.deleteMany(deleteFilter);
	}
	
	public static void validateUser(ClientSession startSession, String email) {
		Document userFilter = new Document(MongoFields.EMAIL, email);
		Document validDoc = new Document(MongoFields.VALIDATED, true);
		Document pushDocument = new Document("$set", validDoc);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		usersCollection.findOneAndUpdate(startSession, userFilter, pushDocument);
	}
	
	//********************************************************************//

	public static MongoCollection<Document> getMongoCollection(String collectionName){
    	MongoDatabase database = getMongoClient().getDatabase(CollectionNames.BOUNTY_BET_DB);
        return database.getCollection(collectionName);
    }
	
	public static MongoClient getMongoClient() {
		if (MONGO_CLIENT == null) {
    		ConnectionString connectionString = new ConnectionString("mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority");
    		MongoClientSettings settings = MongoClientSettings.builder()
    		        .applyConnectionString(connectionString)
    		        .build();
    		MONGO_CLIENT = MongoClients.create(settings);
    		
    		Logger logger = Logger.getLogger("org.mongodb.driver");
    		logger.setLevel(Level.INFO);
    	}
		
		return MONGO_CLIENT;
	}


}
