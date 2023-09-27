package gr.server.mongo.util;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
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

import gr.server.data.bet.enums.PredictionSettleStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;

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
public class MongoUtils {
	
	//final static String conn = "mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority";

	static MongoClient MONGO_CLIENT;

    public static <E> ArrayList<E> get(String collectionString, Bson filter, Executor<E> e){
		MongoCollection<Document> collection = getMongoCollection(collectionString);
		FindIterable<Document> find = collection.find(filter);
		ArrayList<E> list  = new ArrayList<E>();
		for (Document document : find) {
			 String json = document.toJson();
			 E object = (E) e.execute(json);
			 e.tidy(object, document);
			 
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
			 e.tidy(object, document);
			 list.add(object);
		 }
		 return list;
	}
    
	
    
//	/**
//	 * We find all the pending bets.
//	 * For every pending bet we find its predictions.
//	 * If a lost prediction is found, the bet is lost.
//	 * If no lost predictions found, but at least one pending found, the bet is pending.
//	 * If no lost and no pending predictions found, the bet is won.
//	 * If the bet is won we assign the new balance to the user.
//	 * Finally we increase the monthly and overall won and lost predictions and bets.
//	 * 
//	 * @param session
//	 * @throws Exception
//	 */
//    public static void settleOpenBets(ClientSession session) throws Exception {
//    	MongoCollection<Document> betsCollection = getMongoCollection(CollectionNames.BETS);
//    	MongoCollection<Document> predictionsCollection = getMongoCollection(CollectionNames.BET_PREDICTIONS);
//
//		Bson pendingBetsFilter = Filters.eq(MongoFields.BET_STATUS, BetStatus.PENDING.getCode());
//		FindIterable<Document> pendingBets = betsCollection.find(session, pendingBetsFilter);
//		
//		for (Document betDocument : pendingBets) {
//			String betId = betDocument.getObjectId(MongoFields.MONGO_ID).toString();
//			Bson betPredictionsByBetIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, betId);
//			FindIterable<Document> betPredictions = predictionsCollection.find(session, betPredictionsByBetIdFilter);
//			
//			if (!betPredictions.iterator().hasNext()) {
//				throw new Exception("Bet without predictions: " + betId);
//			}
//			
//			int pendingPredictionsCount = 0;
//			int lostPredictionsCount = 0;
//			int wonPredictionsCount = 0;
//			for (Document predictionDocument : betPredictions) {
//				
//				int predictionStatus = predictionDocument.getInteger(MongoFields.USER_BET_PREDICTION_STATUS);
//				
//				if (PredictionStatus.PENDING.getCode() == predictionStatus) {
//					pendingPredictionsCount = pendingPredictionsCount + 1;
//				}else if (PredictionStatus.MISSED.getCode() == predictionStatus) {
//					Bson setLostFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_UNFAVOURABLY.getCode());
//					Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
//					betsCollection.updateOne(session, betByIdFilter, setLostFilter);
//			
//					lostPredictionsCount = lostPredictionsCount + 1;
//				}else if (PredictionStatus.CORRECT.getCode() == predictionStatus){
//					wonPredictionsCount = wonPredictionsCount + 1;
//				}
//			}
//			
//			if (pendingPredictionsCount > 0 ) {
//				return;
//			}
//			
//			if (pendingPredictionsCount == 0 && lostPredictionsCount == 0 && wonPredictionsCount > 0) {
//			
//				Bson setWonFilter = Updates.set(MongoFields.BET_STATUS, BetStatus.SETTLED_FAVOURABLY.getCode());
//				Bson betByIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(betId));
//				betsCollection.updateOne(session, betByIdFilter, setWonFilter);
//				updateUserBalance(session, betDocument.getString(MongoFields.BET_MONGO_USER_ID), betDocument.getDouble(MongoFields.USER_BET_POSSIBLE_WINNINGS));
//			
//			}else if (pendingPredictionsCount == 0 && lostPredictionsCount > 0) {
//			
//				Bson increaseWonOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_EVENTS, wonPredictionsCount);
//				Bson increaseWonMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_EVENTS, wonPredictionsCount);
//				
//				Bson increaseLostOverallPredictionsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_EVENTS, lostPredictionsCount);
//				Bson increaseLostMonthlyPredictionsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_EVENTS, lostPredictionsCount);
//				
//				Bson increaseWonOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_WON_EVENTS, wonPredictionsCount);
//				Bson increaseWonMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_WON_EVENTS, wonPredictionsCount);
//				
//				Bson increaseLostOverallBetsDocument = Updates.inc(MongoFields.USER_OVERALL_LOST_EVENTS, lostPredictionsCount);
//				Bson increaseLostMonthlyBetsDocument = Updates.inc(MongoFields.USER_MONTHLY_LOST_EVENTS, lostPredictionsCount);
//				
//				
//				updateMongoField(session, MongoFields.use, betPredictionsByBetIdFilter, pendingBetsFilter);
//			
//			}
//			
//			
//		}
//    }

   

	public static List<Document> getPredictionsDocuments(UserBet userBet, String newBetMongoId) {
		List<Document> newBetPredictions = new ArrayList<>();

		List<UserPrediction> predictions = userBet.getPredictions();
		for (UserPrediction prediction : predictions) {
			Document newBetPrediction = new Document(MongoFields.EVENT_ID, prediction.getEventId())
					.append(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, newBetMongoId)
					.append(MongoFields.BET_MONGO_USER_ID, userBet.getMongoUserId())
					.append(MongoFields.USER_BET_PREDICTION_TYPE, prediction.getPredictionType().getCode())
					.append(MongoFields.USER_BET_PREDICTION_CATEGORY, prediction.getPredictionCategory().getCategoryCode())
					.append(MongoFields.USER_BET_PREDICTION_STATUS, PredictionStatus.PENDING.getCode())
					.append(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, PredictionSettleStatus.UNSETTLED.getCode())
					.append(MongoFields.USER_BET_PREDICTION_ODD_VALUE, prediction.getOddValue()

					);
			newBetPredictions.add(newBetPrediction);
		}
		
		return newBetPredictions;
	}

	public static Document getBetDocument(UserBet userBet) {
		return new Document(MongoFields.BET_MONGO_USER_ID, userBet.getMongoUserId())
				.append(MongoFields.BET_AMOUNT, userBet.getBetAmount())
				.append(MongoFields.BET_STATUS, userBet.getBetStatus().getCode())
				.append(MongoFields.BET_BELONGING_MONTH, userBet.getBelongingMonth())
				.append(MongoFields.BET_BELONGING_YEAR, userBet.getBelongingYear())
				.append(MongoFields.USER_BET_PLACEMENT_MILLIS, userBet.getBetPlaceMillis())
				.append(MongoFields.USER_BET_POSSIBLE_WINNINGS, userBet.getPossibleEarnings())
				;
	}

	public static Document getNewUserDocument(User user) {
		 return new Document(MongoFields.USERNAME, user.getUsername())
				 .append(MongoFields.EMAIL, user.getEmail())
				 .append(MongoFields.PASSWORD, user.getPassword())
				 .append(MongoFields.VALIDATED, false)
				 
				 .append(MongoFields.USER_MONTHLY_LOST_EVENTS, 0)
				 .append(MongoFields.USER_MONTHLY_WON_EVENTS, 0)
				 .append(MongoFields.USER_MONTHLY_LOST_SLIPS, 0)
				 .append(MongoFields.USER_MONTHLY_WON_SLIPS, 0)
				 
				 .append(MongoFields.USER_OVERALL_LOST_EVENTS, 0)
				 .append(MongoFields.USER_OVERALL_WON_EVENTS, 0)
				 .append(MongoFields.USER_OVERALL_LOST_SLIPS, 0)
				 .append(MongoFields.USER_OVERALL_WON_SLIPS, 0)
				 
		 .append(MongoFields.USER_BALANCE, SportScoreApiConstants.STARTING_BALANCE)
		 .append(MongoFields.USER_AWARDS, new BasicDBList());
	}
	
	public static Document getSettledEventDocument(int eventId) {
		
		int belongingDay = LocalDate.now().getDayOfMonth();
		int belongingMonth = LocalDate.now().getMonthValue();//TODO what if some events span to another month?
		int belongingYear = LocalDate.now().getYear();
		
		 return new Document(MongoFields.EVENT_ID, eventId)
				 .append(MongoFields.SETTLED_EVENT_BELONGING_DAY, belongingDay)
				 .append(MongoFields.SETTLED_EVENT_BELONGING_MONTH, belongingMonth)
				 .append(MongoFields.SETTLED_EVENT_BELONGING_YEAR, belongingYear)
				;
	}

	public static Document getAndOrDocument(
			List<Document> possibleValues, String andOr) {
		BasicDBList orList = new BasicDBList();
		for (Document document : possibleValues){
			orList.add(document);
		}
		return new Document(andOr, orList);
	}
	
	public static void updateMongoField(ClientSession session, String collection, Bson findFilter, Bson updateFilter) {
		MongoCollection<Document> usersCollection = getMongoCollection(collection);
		usersCollection.findOneAndUpdate(session, findFilter, updateFilter);
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

//	public static void deleteUserBetsFor(ClientSession startSession, String pastMonthAsString) {
//		Document belongingMonthFilter = new Document(MongoFields.BET_BELONGING_MONTH, pastMonthAsString);
//		MongoCollection<Document> betsCollection = getMongoCollection(CollectionNames.BETS);
//		betsCollection.deleteMany(startSession, belongingMonthFilter);
//	}
	
	public static void deleteMany(String collectionName, Document deleteFilter) {
		MongoCollection<Document> collection = getMongoCollection(collectionName);
		collection.deleteMany(deleteFilter);
	}
	

	//********************************************************************//

	public static MongoCollection<Document> getMongoCollection(String collectionName){
    	MongoDatabase database = getMongoClient().getDatabase(CollectionNames.BOUNTY_BET_DB);
        return database.getCollection(collectionName);
    }
	
	public static MongoClient getMongoClient() {
		if (MONGO_CLIENT == null) {
    		ConnectionString connectionString = new ConnectionString("mongodb+srv://bountyBetUser:a7fdy4hTXZWeL1kP@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority");
    		MongoClientSettings settings = MongoClientSettings.builder()
    		        .applyConnectionString(connectionString)
    		        .build();
    		MONGO_CLIENT = MongoClients.create(settings);
    		
//    		Logger logger = Logger.getLogger("org.mongodb.driver");
//    		logger.setLevel(Level.INFO);
    	}
		
		return MONGO_CLIENT;
	}


}
