package gr.server.mongo.util;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
//import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.Team;
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
    
	public static List<Document> getPredictionsDocuments(UserBet userBet, String newBetMongoId) {
		
		
		List<Document> newBetPredictions = new ArrayList<>();

		List<UserPrediction> predictions = userBet.getPredictions();
		for (UserPrediction prediction : predictions) {
			
			MatchEvent matchEvent = FootballApiCache.ALL_EVENTS.get(prediction.getEventId());

			Document newBetPrediction = new Document(MongoFields.EVENT_ID, prediction.getEventId())
					.append(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, newBetMongoId)
					.append(MongoFields.BET_MONGO_USER_ID, userBet.getMongoUserId())
					.append(MongoFields.USER_BET_PREDICTION_TYPE, prediction.getPredictionType().getCode())
					.append(MongoFields.USER_BET_PREDICTION_CATEGORY, prediction.getPredictionCategory().getCategoryCode())
					.append(MongoFields.USER_BET_PREDICTION_STATUS, PredictionStatus.PENDING.getCode())
					.append(MongoFields.USER_BET_PREDICTION_SETTLE_STATUS, PredictionSettleStatus.UNSETTLED.getCode())
					.append(MongoFields.USER_BET_PREDICTION_ODD_VALUE, prediction.getOddValue())
					.append(MongoFields.SPORT_ID, matchEvent.getSport_id())
					.append(MongoFields.USER_BET_PREDICTION_HOME_TEAM_ID, matchEvent.getHome_team().getId())//TODO: stored to avoid calls to API.
					.append(MongoFields.USER_BET_PREDICTION_AWAY_TEAM_ID, matchEvent.getAway_team().getId());
			
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
	
	public static Document getTeamDocument(Team team) {
		
		BasicDBObject translations = new BasicDBObject(team.getName_translations());
		
		 return new Document(MongoFields.TEAM_ID, team.getId())
					 .append(MongoFields.TEAM_NAME, team.getName())
					 .append(MongoFields.TEAM_SPORT_ID, team.getSport_id())
					 .append(MongoFields.TEAM_LOGO_URL, team.getLogo())
					 .append(MongoFields.TRANSLATIONS, translations);
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
    		
    	}
		
		Logger logger = Logger.getLogger("org.mongodb.driver");
		logger.setLevel(Level.INFO);
		
		return MONGO_CLIENT;
	}


}
