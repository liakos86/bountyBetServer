package gr.server.impl.client;

import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.events.Event;
import gr.server.data.api.model.league.League;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.bet.enums.PredictionValue;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.Fields;
import gr.server.data.constants.ApiFootBallConstants;
import gr.server.data.user.model.User;
import gr.server.data.user.model.UserAward;
import gr.server.data.user.model.UserBet;
import gr.server.data.user.model.UserPrediction;
import gr.server.def.client.MongoClientHelper;
import gr.server.mongo.application.Mongo;
import gr.server.mongo.util.DateUtils;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.MongoCollectionUtils;
import gr.server.transaction.helper.TransactionalBlock;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoClientHelperImpl 
implements MongoClientHelper {
	
	
	/*
	 * PUBLIC API
	 * 
	 */
	
	@Override
	public User createUser(User user) throws UserExistsException {
		MongoClient client = Mongo.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB).getCollection(CollectionNames.USERS);
		
		 Document existingUser = new Document("username", user.getUsername());
		 FindIterable<Document> find = users.find(existingUser);
		 
		 Document first = find.first();
		 if (first != null){
			 throw new UserExistsException("User " + user.getUsername() + " already exists");
		 }
		 
		 Document newUser = MongoCollectionUtils.getNewUserDocument(user.getUsername());
		 users.insertOne(newUser);
		 
		User createdUser = new User(newUser.getObjectId("_id").toString());
		createdUser.setUsername(newUser.getString("username"));
		createdUser.setPosition(MongoCollectionUtils.userPosition(createdUser));
		return createdUser;
	}

	@Override
	public User getUser(String id) {
		MongoClient client = Mongo.getMongoClient();
		MongoCollection<Document> users = client.getDatabase(CollectionNames.BOUNTY_BET_DB).getCollection(CollectionNames.USERS);
		FindIterable<Document> usersFromMongo = users.find(new Document(Fields.MONGO_ID, new ObjectId(id)));
		
		if (! usersFromMongo.iterator().hasNext()){
			return null;
		}
		
		Document userFromMongo = usersFromMongo.iterator().next();
		String userString = userFromMongo.toJson();
		User finalUser = new Gson().fromJson(userString, new TypeToken<User>() {}.getType());
		finalUser.setUserBets(getBetsForUser(id));
		finalUser.setUserAwards(getAwardsForUser(id));
		finalUser.setMongoId(userFromMongo.getObjectId(Fields.MONGO_ID).toString());
		finalUser.setPosition(MongoCollectionUtils.userPosition(finalUser));
		return finalUser;
	}
	
	@Override
	public void storeLeagues(ClientSession session, List<League> leagues) throws ParseException {
		List<Document> newLeagues = new ArrayList<Document>();
			for (League league : leagues) {
				Document newLeague = MongoCollectionUtils.getLeagueDocument(league);
				newLeagues.add(newLeague);
				
				new MongoClientHelperImpl().storeEvents(session, league.getEvents());
				
			}
			
//			SupportedCountry supportedLeague = entry.getKey();
//			Document newSupportedLeague = MongoCollectionUtils.getSupportedLeagueDocument(supportedLeague, newComps);
//			supportedLeagues.add(newSupportedLeague);
		
		MongoClient client = Mongo.getMongoClient();
		MongoCollection<Document> leaguesCollection = client.getDatabase(CollectionNames.BOUNTY_BET_DB).getCollection(CollectionNames.LEAGUES);
		leaguesCollection.insertMany(session, newLeagues);
	}

	@Override
	public List<League> getLeagues() {
		Executor<League> executor = new Executor<League>(new TypeToken<League>() { });
		List<League> leagues = MongoCollectionUtils.get(CollectionNames.LEAGUES, new Document(), executor);
		for (League league : leagues){
				Document eventsFilter = new Document("league_id", league.getLeagueId()).append("country_id", league.getCountryId());
				Executor<Event> eventsExecutor = new Executor<Event>(new TypeToken<Event>() { });
				List<Event> leagueEvents = MongoCollectionUtils.get(CollectionNames.EVENTS, eventsFilter, eventsExecutor);
				league.setEvents(leagueEvents);
		}
		
		return leagues;
	}
	
	@Override
	public UserBet placeBet(UserBet userBet) {
		MongoClient client = Mongo.getMongoClient();
		MongoDatabase database = client.getDatabase(CollectionNames.BOUNTY_BET_DB);
		MongoCollection<Document> betsCollection = database.getCollection(CollectionNames.BETS);
		Document newBet = MongoCollectionUtils.getBetDocument(userBet);
		betsCollection.insertOne(newBet);
		userBet.setMongoId(newBet.getObjectId(Fields.MONGO_ID).toString()); 
		MongoCollectionUtils.updateUser(userBet);
		return userBet;
	}
	
	@Override
	public void settleBets(){
		List<Document> possibleValues = Arrays.asList(new Document[]{new Document("betStatus", BetStatus.PENDING.getCode()), new Document("betStatus", BetStatus.PENDING_LOST.getCode())});
		Document openPendingBetsFilter = MongoCollectionUtils.getOrDocument("betStatus", possibleValues);
		
		Executor<UserBet> executor = new Executor<UserBet>(new TypeToken<UserBet>() { });
		List<UserBet> openBets = MongoCollectionUtils.get(CollectionNames.BETS, openPendingBetsFilter, executor);

		Document eventFilter = new Document("match_status", "FT");
		Executor<Event> executorEvent = new Executor<Event>(new TypeToken<Event>() { });
		List<Event> finished = MongoCollectionUtils.get(CollectionNames.EVENTS, eventFilter, executorEvent);

		Map<String, Event> finishedEvents = new HashMap<String, Event>();
		for (Event event : finished){
			finishedEvents.put(event.getMatchId(), event);
		}
		
		for (UserBet userBet : openBets){
			settleBet(userBet, finishedEvents);
		}
	}
	
	//END OF PUBLIC API
	
	void settleBet(UserBet userBet, Map<String, Event> finishedEvents) {
		for (UserPrediction prediction : userBet.getPredictions()){
			if (PredictionStatus.PENDING.getCode() != prediction.getPredictionStatus()){
				continue;
			}
			
			Event predictionEvent = finishedEvents.get(prediction.getEventId());
			PredictionValue result = calculateEventResult(predictionEvent);
			if (result.getCode() == prediction.getPrediction()){
				prediction.setPredictionStatus(PredictionStatus.CORRECT.getCode());
			}else{
				prediction.setPredictionStatus(PredictionStatus.MISSED.getCode());
				userBet.setBetStatus(BetStatus.PENDING_LOST.getCode());
			}
		}
		
		if (shouldSettleFavourably(userBet)){
			settleFavourably(userBet);
		}
		
		if (shouldSettleUnfavourably(userBet)){
			settleUnfavourably(userBet);
		}
		
		userBet = MongoCollectionUtils.updateBet(userBet);
		MongoCollectionUtils.updateUser(userBet);
	}

	void settleUnfavourably(UserBet userBet) {
		userBet.setBetStatus(BetStatus.SETTLED_INFAVOURABLY.getCode());
	}
	
	void settleFavourably(UserBet userBet) {
		userBet.setBetStatus(BetStatus.SETTLED_FAVOURABLY.getCode());
	}

	PredictionValue calculateEventResult(Event predictionEvent) {
		Integer matchHometeamScore = Integer.parseInt(predictionEvent.getMatchHometeamScore());
		Integer matchAwayTeamScore = Integer.parseInt(predictionEvent.getMatchAwayteamScore());
		PredictionValue predictionEventResult = matchHometeamScore > matchAwayTeamScore ? PredictionValue.HOME : (matchHometeamScore.equals(matchAwayTeamScore) ? PredictionValue.DRAW : PredictionValue.AWAY);
		return predictionEventResult;
	}

	/**
	 * If user missed one prediction, the {@link UserBet} is considered lost.
	 * 
	 */
	boolean shouldSettleUnfavourably(UserBet userBet) {
		boolean atLeastOneMissed = false;
		for (UserPrediction prediction : userBet.getPredictions()){
			if (PredictionStatus.PENDING.getCode() == prediction.getPredictionStatus()){
				return false;
			}
			if (PredictionStatus.MISSED.getCode() == prediction.getPredictionStatus()){
				atLeastOneMissed = true;
			}
		}
		
		if (atLeastOneMissed){
			return true;
		}
		return false;
	}

	boolean shouldSettleFavourably(UserBet userBet) {
		for (UserPrediction prediction : userBet.getPredictions()){
			if (PredictionStatus.CORRECT.getCode() != prediction.getPredictionStatus()){
				return false;
			}
		}
		return true;
	}

	public List<UserBet> getBetsForUser(String userId) {
		Document userBetsFilter = new Document(Fields.USER_ID, userId);
		Executor<UserBet> betsExecutor = new Executor<UserBet>(new TypeToken<UserBet>() { });
		List<UserBet> bets = MongoCollectionUtils.get(CollectionNames.EVENTS, userBetsFilter, betsExecutor);
		 return bets;
	}
	
	public List<UserAward> getAwardsForUser(String userId) {
		Document userAwardsFilter = new Document(Fields.USER_ID, userId);
		Executor<UserAward> awardsExecutor = new Executor<UserAward>(new TypeToken<UserAward>() { });
		List<UserAward> awards = MongoCollectionUtils.get(CollectionNames.EVENTS, userAwardsFilter, awardsExecutor);
		 return awards;
	}

	public List<User> retrieveLeaderBoard() {
		Document sortField = new Document(Fields.USER_BALANCE, -1);
		Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() { });
		List<User> users = MongoCollectionUtils.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 20);
		return users;
	}
	
	/**
	 * Every event is first deleted and then alltogether inserted into the events collection.
	 * In case event is not existent nothing is deleted.
	 * 
	 * @param session
	 * @param events
	 * @throws ParseException
	 */
	public void storeEvents(ClientSession session, List<Event> events) throws ParseException {
		
		if (events.size() == 0){
			return;
		}
		
		MongoClient client = Mongo.getMongoClient();
		MongoCollection<Document> eventsCollection = client.getDatabase(CollectionNames.BOUNTY_BET_DB).getCollection(CollectionNames.EVENTS);

		List<Document> newEvents = new ArrayList<Document>();
		for (Event event : events) {
			Document match_by_id = new Document(Fields.MATCH_ID, event.getMatchId());
			eventsCollection.deleteOne(session, match_by_id);
			
			Document newEvent = MongoCollectionUtils.getEventDocument(event);
			newEvents.add(newEvent);
		}
		
		eventsCollection.insertMany(session, newEvents);
	}
	
	/**
	 * Runs on the midnight of the first day of the month.
	 * Finds the winner.
	 * Stores an award.
	 * Updates the winner's fields.
	 * Restores every user's balance to {@link ApiFootBallConstants#STARTING_BALANCE}.
	 * Deletes bets going 2 months ago.
	 * @throws Exception 
	 * 
	 * @see TimerTaskHelper#getMonthChangeCheckerTask()
	 */
	public void settleMonthlyAward(String monthToSettle) throws Exception{
		
		new TransactionalBlock() {
			@Override
			public void begin() {
				Document sortField = new Document(Fields.USER_BALANCE, -1);
				Executor<User> usersExecutor = new Executor<User>(new TypeToken<User>() { });
				List<User> monthWinners = MongoCollectionUtils.getSorted(CollectionNames.USERS, usersExecutor, new Document(), sortField, 1);
				//TODO tied users
				Document awardDocument = MongoCollectionUtils.createAwardFor(session, monthWinners.get(0));
				MongoCollectionUtils.updateUserAwards(session, monthWinners.get(0), awardDocument.getObjectId(Fields.MONGO_ID));
			
				MongoCollectionUtils.restoreUserBalance(session);
				MongoCollectionUtils.deleteUserBetsFor(session, DateUtils.getPastMonthAsString(2));
				
			}
		}.execute();
	}
	
	/**
	 * Delete X days ago based on event start time.
	 */
	public void deletePastEvents(){
		Document lessThanDate = new Document("$lt", DateUtils.getStaleEventsDate().getTime());
		Document staleEventsFilter = new Document(Fields.MATCH_FULL_DATE, lessThanDate);
		MongoCollectionUtils.deleteMany(CollectionNames.EVENTS, staleEventsFilter);
	}

	public void deleteBountiesUntil(ClientSession session,
			Date bountiesExpirationDate) {
		// TODO Auto-generated method stub
		
	}
	
}
