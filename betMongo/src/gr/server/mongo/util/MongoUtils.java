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

import gr.server.common.MongoCollectionConstants;
import gr.server.common.ServerConstants;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.PredictionSettleStatus;
import gr.server.data.bet.enums.PredictionStatus;
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
	
	public static volatile boolean DB_DATA_FETCHED = false;

	static MongoClient MONGO_CLIENT;
	

    public static <E> ArrayList<E> get(ClientSession session, String collectionString, Bson filter, Executor<E> e){
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
    
    public static <E> ArrayList<E> getSorted(ClientSession session, String collectionString,  Executor<E> e, Bson findFilter, Document sortFilter, int limit){
		 MongoCollection<Document> collection = getMongoCollection(collectionString);
		 FindIterable<Document> find = collection.find(session, findFilter).limit(limit).sort(sortFilter);
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
					
					.append(MongoFields.MONGO_USER_ID, userBet.getMongoUserId())
					.append(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, newBetMongoId)
					
					.append(MongoFields.USER_BET_PREDICTION_BET_LEAGUE_ID, matchEvent.getLeague_id())
					.append(MongoFields.USER_BET_PREDICTION_BET_START_AT, matchEvent.getStart_at())

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
		return new Document(MongoFields.MONGO_USER_ID, userBet.getMongoUserId())
				.append(MongoFields.BET_AMOUNT, userBet.getBetAmount())
				.append(MongoFields.BET_STATUS, userBet.getBetStatus())
				.append(MongoFields.BET_BELONGING_MONTH, userBet.getBelongingMonth())
				.append(MongoFields.BET_BELONGING_YEAR, userBet.getBelongingYear())
				.append(MongoFields.USER_BET_PLACEMENT_MILLIS, userBet.getBetPlacementMillis())
				;
	}
	
	public static Document getSectionDocument(Section section) {
        return new Document(MongoFields.ID, section.getId())
                .append("sport_id", section.getSport_id())
//                .append("slug", section.getSlug())
                .append("name", section.getName())
                .append("priority", section.getPriority())
                .append("flag", section.getFlag())
                .append("name_translations", section.getName_translations());
    }
	
	public static Document getLeagueDocument(League l) {
        return new Document(MongoFields.ID, l.getId())
                .append("sport_id", l.getSport_id())
                .append("section_id", l.getSection_id())
                .append("name", l.getName())
                .append("name_translations", l.getName_translations())
                .append("logo", l.getLogo())
                .append("priority", l.getPriority())
                .append("start_date", l.getStart_date())
                .append("end_date", l.getEnd_date())
                .append("seasonIds", l.getSeasonIds());
    }
	
//	// Method to populate Section from a MongoDB Document
//    public static Section getSectionFromDocument(Document doc) {
//        Section section = new Section();
//        section.setId(doc.getInteger(MongoFields.SECTION_ID, 0));
//        section.setSport_id( doc.getInteger("sport_id", 0));
//        section.setName( doc.getString("name"));
//        section.setPriority( doc.getInteger("priority", 0));
//        section.setFlag( doc.getString("flag"));
//        section.setName_translations( doc.get("name_translations", Map.class)); // Cast map from the document
//        return section;
//    }
	
	
	
	
	public static Document getMonthlyBalanceDocument(String mongoUserId, int month) {
		return new Document(MongoFields.MONGO_USER_ID, mongoUserId)
				.append(MongoFields.USER_BALANCE_MONTH, month)
				.append(MongoFields.USER_BALANCE, ServerConstants.STARTING_BALANCE)

				.append(MongoFields.USER_BALANCE_BET_AMOUNT_MONTHLY, 0)
				.append(MongoFields.USER_MONTHLY_LOST_EVENTS, 0)
				 .append(MongoFields.USER_MONTHLY_WON_EVENTS, 0)
				 .append(MongoFields.USER_MONTHLY_LOST_SLIPS, 0)
				 .append(MongoFields.USER_MONTHLY_WON_SLIPS, 0)
				;
	}

	public static Document getNewUserDocument(User user) {
		 return new Document(MongoFields.USERNAME, user.getUsername())
				 .append(MongoFields.EMAIL, user.getEmail())
				 .append(MongoFields.PASSWORD, user.getPassword())
				 .append(MongoFields.VALIDATED, false)
				 
				 			 
				 .append(MongoFields.USER_BET_AMOUNT_OVERALL, 0)
				 .append(MongoFields.USER_OVERALL_LOST_EVENTS, 0)
				 .append(MongoFields.USER_OVERALL_WON_EVENTS, 0)
				 .append(MongoFields.USER_OVERALL_LOST_SLIPS, 0)
				 .append(MongoFields.USER_OVERALL_WON_SLIPS, 0);
	}
	
	public static Document getTeamDocument(Team team) {
		
		BasicDBObject translations = new BasicDBObject(team.getName_translations());
		
		 return new Document(MongoFields.ID, team.getId())
					 .append(MongoFields.NAME, team.getName())
					 .append(MongoFields.SPORT_ID, team.getSport_id())
					 .append(MongoFields.LOGO_URL, team.getLogo())
					 .append(MongoFields.TRANSLATIONS, translations);
	}
	
	public static Document getAwardDocument(String userId, Double balance, int month, int year) {
		 return new Document(MongoFields.MONGO_USER_ID, userId)
					 .append(MongoFields.AWARD_MONTH, month)
					 .append(MongoFields.AWARD_YEAR, year)
					 .append(MongoFields.AWARD_PLACEMENT, System.currentTimeMillis())
					 .append(MongoFields.AWARD_BALANCE, balance);
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

	public static void restoreUserBalance(ClientSession startSession) {
		Document balanceFilter = new Document(MongoFields.USER_BALANCE, SportScoreApiConstants.STARTING_BALANCE);
		Document setBalance = new Document("$set", balanceFilter);
		MongoCollection<Document> usersCollection = getMongoCollection(MongoCollectionConstants.USERS);
		usersCollection.updateMany(startSession, new Document(), setBalance);
	}

	public static void deleteMany(String collectionName, Document deleteFilter) {
		MongoCollection<Document> collection = getMongoCollection(collectionName);
		collection.deleteMany(deleteFilter);
	}
	

	//********************************************************************//

	public static MongoCollection<Document> getMongoCollection(String collectionName){
    	MongoDatabase database = getMongoClient().getDatabase(MongoCollectionConstants.BOUNTY_BET_DB);
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
