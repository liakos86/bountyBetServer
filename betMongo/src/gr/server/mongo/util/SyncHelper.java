package gr.server.mongo.util;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
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

import gr.server.data.api.model.events.Event;
import gr.server.data.api.model.league.League;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.constants.ApiFootBallConstants;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.Fields;
import gr.server.data.user.model.User;
import gr.server.data.user.model.UserBet;
import gr.server.data.user.model.UserPrediction;

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
	
   // final static String conn = "mongodb://liakos86:art78tha3m@spearo-shard-00-00.tgnig.gcp.mongodb.net:27017,spearo-shard-00-01.tgnig.gcp.mongodb.net:27017,spearo-shard-00-02.tgnig.gcp.mongodb.net:27017";
    
	final static String conn = "mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority";
	static MongoClient MONGO_CLIENT;

    public static <E> ArrayList<E> get(String collectionString, Document filter, Executor<E> e){
		MongoCollection<Document> collection = getMongoCollection(collectionString);
		FindIterable<Document> find = collection.find(filter);
		ArrayList<E> list  = new ArrayList<E>();
		for (Document document : find) {
			 String json = document.toJson();
			 E object = (E) e.execute(json);
			 if (object instanceof UserBet){
				 ((UserBet)object).setMongoId(document.getObjectId(Fields.MONGO_ID).toString());
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
				((User) object).setMongoId(document.getObjectId(Fields.MONGO_ID).toString()); 
			 }
			list.add(object);
		 }
		 return list;
	}
	
	public static UserBet updateBet(UserBet userBet){
		MongoCollection<Document> collection = getMongoCollection(CollectionNames.BETS);
		Document filter = new Document(Fields.MONGO_ID, new ObjectId(userBet.getMongoId()) );
		Document updateFieldDocument = new Document("betStatus", userBet.getBetStatus());
		for (int i =0; i < userBet.getPredictions().size(); i++){
			updateFieldDocument.append("predictions."+String.valueOf(i)+".predictionStatus", userBet.getPredictions().get(i).getPredictionStatus());
		}
		Document allFieldsDocument = new Document("$set", updateFieldDocument);	 
		collection.findOneAndUpdate(filter, allFieldsDocument);
		
		return userBet;
	}
	
	Document betDocument(UserBet userBet) {
		Document newBet = new Document("mongoUserId", userBet.getMongoUserId())
				.append("betAmount", userBet.getBetAmount())
				.append("betStatus", userBet.getBetStatus())
				.append(Fields.BET_PLACE_DATE, userBet.getBetPlaceDate());

		BasicDBList newBetPredictions = new BasicDBList();

		for (UserPrediction prediction : userBet.getPredictions()) {
			Document newBetPrediction = new Document("eventId",
					prediction.getEventId())
					.append("prediction", prediction.getPrediction())
					.append("predictionDescription",
							prediction.getPredictionDescription())
					.append("oddValue", prediction.getOddValue()

					);
			newBetPredictions.add(newBetPrediction);
		}

		newBet.append("predictions", newBetPredictions);

		return newBet;
	}

	/**
	 * {@link User} will be updated depending on his won/lost {@link UserBet}.
	 * 
	 * @param userBet
	 */
	public static void updateUser(UserBet userBet) {
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		Document filter = new Document(Fields.MONGO_ID, new ObjectId(userBet.getMongoUserId()));

		Document userFieldsDocument = new Document();
		if (userBet.getBetStatus() == BetStatus.SETTLED_FAVOURABLY.getCode()){//bet won
			userFieldsDocument.append("wonSlipsCount", 1)
			.append("wonEventsCount", userBet.getPredictions().size())
			.append(Fields.USER_BALANCE, userBet.getPossibleEarnings());
		}else if (userBet.getBetStatus() == BetStatus.SETTLED_INFAVOURABLY.getCode()){//bet lost
			int correctPredictions = numOfSuccessFullPredictions(userBet);
			userFieldsDocument.append("lostSlipsCount", 1)
			.append("wonEventsCount", correctPredictions)
			.append("lostEventsCount", userBet.getPredictions().size() - correctPredictions);
		}else if (userBet.getBetStatus() == BetStatus.PENDING.getCode()){// bet placed
			userFieldsDocument.append(Fields.USER_BALANCE, -1 * (userBet.getBetAmount()));
		}
		Document increaseOrDecreaseDocument = new Document("$inc", userFieldsDocument);
		usersCollection.findOneAndUpdate(filter, increaseOrDecreaseDocument);
	}
	
	static int numOfSuccessFullPredictions(UserBet userBet){
		int won =0;
		for(UserPrediction prediction : userBet.getPredictions()){
			if (prediction.getPredictionStatus() == PredictionStatus.CORRECT.getCode()){
				++won;
			}
		}
		return won;
	}

	/**
	 * The trick here is that we save the match's date+time in its milliseconds representation.
	 * The ApiFootball serves the GMT of every match, so we can make the appropriate transformation 
	 * in each device later on.
	 * Also this serves as a comparison field in order to perform deletions based on the  date.
	 * 
	 * @param event
	 * @return
	 * @throws ParseException
	 */
	public static Document getEventDocument(Event event) throws ParseException {
		//Odd odd = event.getOdd();
		Document newOdd = new Document("odd_1", "1,8")// odd.getOdd1())
		.append("odd_2", "3,3")// odd.getOdd2())
		.append("odd_x", "3,6");//odd.getOddX());
		
		Document newEvent = new Document(Fields.MATCH_ID, event.getMatchId())
		.append("league_id", event.getLeagueId())
		.append("country_id", event.getCountryId())
		.append("match_hometeam_name", event.getMatchHometeamName())
		.append("match_awayteam_name", event.getMatchAwayteamName())
		.append(Fields.MATCH_FULL_DATE, event.getEventMillis()) 
		.append("match_date", event.getMatchDate())
		.append("match_time", event.getMatchTime())
		.append("match_live", event.getMatchLive())
		.append("match_status", event.getMatchStatus())
		.append("match_hometeam_score", event.getMatchHometeamScore())
		.append("match_awayteam_score", event.getMatchAwayteamScore())
		.append("match_hometeam_extra_score", event.getMatchHometeamScore())
		.append("match_awayteam_extra_score", event.getMatchAwayteamScore())
		.append("odd", newOdd);
		
		return newEvent;
		
	}

	public static Document getBetDocument(UserBet userBet) {
		Document newBet = new Document(Fields.BET_MONGO_USER_ID, userBet.getMongoUserId())
				.append(Fields.BET_AMOUNT, userBet.getBetAmount())
				.append(Fields.BET_STATUS, userBet.getBetStatus())
				.append(Fields.BET_PLACE_DATE, userBet.getBetPlaceDate())
				.append(Fields.BET_BELONGING_MONTH, DateUtils.getPastMonthAsString(0));

		BasicDBList newBetPredictions = new BasicDBList();

		for (UserPrediction prediction : userBet.getPredictions()) {
			Document newBetPrediction = new Document("eventId",
					prediction.getEventId())
					.append("prediction", prediction.getPrediction())
					.append("predictionDescription",
							prediction.getPredictionDescription())
					.append("oddValue", prediction.getOddValue()

					);
			newBetPredictions.add(newBetPrediction);
		}

		newBet.append("predictions", newBetPredictions);
		return newBet;
	}

	public static Document getNewUserDocument(String userName) {
		 return new Document("username", userName)
		 .append("wonEventsCount", 0)
		 .append("lostEventsCount", 0)
		 .append("wonSlipsCount", 0)
		 .append("lostSlipsCount", 0)
		 .append(Fields.USER_OVERALL_WON_EVENTS, 0)
		 .append(Fields.USER_OVERALL_LOST_EVENTS, 0)
		 .append(Fields.USER_OVERALL_WON_SLIPS, 0)
		 .append(Fields.USER_OVERALL_LOST_SLIPS, 0)
		 .append(Fields.USER_BALANCE, ApiFootBallConstants.STARTING_BALANCE)
		 .append(Fields.USER_AWARDS, new BasicDBList());
	}

	public static Document getLeagueDocument(League competition) {
		return new Document("league_id", competition.getLeagueId())
		 .append("country_id", competition.getCountryId())
		 .append("league_name", competition.getLeagueName())
		 .append("country_name", competition.getCountryName());
	}

	public static Document getOrDocument(String string,
			List<Document> possibleValues) {
		BasicDBList orList = new BasicDBList();
		for (Document document : possibleValues){
			orList.add(document);
		}
		return new Document("$or", orList);
	}
	
	public static long userPosition(User user){
		Document userBalance = new Document("$gt", user.getBalance());
		Document greaterBalancedUsers = new Document(Fields.USER_BALANCE, userBalance);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		return usersCollection.countDocuments(greaterBalancedUsers);
	}

	public static Document createAwardFor(ClientSession startSession, User monthWinner) {
		Document awardDocument = new Document();
		awardDocument.append(Fields.AWARD_WINNER, monthWinner.getMongoId())
		.append(Fields.AWARD_MONTH, DateUtils.getPastMonthAsString(0))
		.append(Fields.AWARD_BALANCE, monthWinner.getBalance());
		MongoCollection<Document> awardsCollection = getMongoCollection(CollectionNames.AWARDS);
		awardsCollection.insertOne(startSession, awardDocument);
		return awardDocument;
	}

	public static void updateUserAwards(ClientSession startSession, User monthWinner, ObjectId awardId) {
		Document userFilter = new Document(Fields.USER_ID, monthWinner.getMongoId());
		Document newAwardDocument = new Document(Fields.USER_AWARDS_IDS, awardId.toString());
		Document pushDocument = new Document("$push", newAwardDocument);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		usersCollection.findOneAndUpdate(startSession, userFilter, pushDocument);
	}

	public static void restoreUserBalance(ClientSession startSession) {
		Document balanceFilter = new Document(Fields.USER_BALANCE, ApiFootBallConstants.STARTING_BALANCE);
		Document setBalance = new Document("$set", balanceFilter);
		MongoCollection<Document> usersCollection = getMongoCollection(CollectionNames.USERS);
		usersCollection.updateMany(startSession, new Document(), setBalance);
	}

	public static void deleteUserBetsFor(ClientSession startSession, String pastMonthAsString) {
		Document belongingMonthFilter = new Document(Fields.BET_BELONGING_MONTH, pastMonthAsString);
		MongoCollection<Document> betsCollection = getMongoCollection(CollectionNames.BETS);
		betsCollection.deleteMany(startSession, belongingMonthFilter);
	}
	
	public static void deleteMany(String collectionName, Document deleteFilter) {
		MongoCollection<Document> collection = getMongoCollection(collectionName);
		collection.deleteMany(deleteFilter);
	}
	
	//********************************************************************//


   // @SuppressWarnings("resource")
	static MongoCollection<Document> getMongoCollection(String collectionName){
    	getMongoClient();
    	
    	MongoDatabase database = MONGO_CLIENT.getDatabase(CollectionNames.BOUNTY_BET_DB);
        return database.getCollection(collectionName);
    }
	
	public static MongoClient getMongoClient() {
		if (MONGO_CLIENT == null) {
    		
    		ConnectionString connectionString = new ConnectionString("mongodb+srv://bountyBetUser:pf4dot4xNL7DBtsX@bountybetcluster.27d3j.mongodb.net/?retryWrites=true&w=majority");
    		MongoClientSettings settings = MongoClientSettings.builder()
    		        .applyConnectionString(connectionString)
    		        .build();
    		MONGO_CLIENT = MongoClients.create(settings);
    		
//    		MongoClientURI uri = new MongoClientURI(conn + "<dbname>?ssl=true&replicaSet=spearo-shard-0&authSource=admin&retryWrites=true&w=majority");
//    		mongoClient = new MongoClient(uri);
    	}
		
		return MONGO_CLIENT;
	}

}
