package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidents;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionType;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.ServerConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class TestMongoClientHelper {
	
	
	/**
	 * Creates a new user.
	 * Places a bet for the new user.
	 * Reads the user again to verify bet fields.
	 * Deletes the user.
	 * Verifies deletion.
	 */
	@Test
	public void testPlaceBet_success(){

		User user = createUser();
		
		Integer matchEventId = 395975;
		FootballApiCache.ALL_EVENTS.put(matchEventId, createEvent(matchEventId, MatchEventStatus.NOTSTARTED));
		
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		UserPrediction pred = new UserPrediction();
		pred.setEventId(matchEventId);
		pred.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		pred.setPredictionType(PredictionType.AWAY_WIN);
		pred.setOddValue(13.5d);
		preds.add(pred);
		
		
		userBet.setPredictions(preds);
		BetPlacementStatus betPlacementStatus = mHelper.placeBet(userBet);
		Assert.assertEquals(BetPlacementStatus.PLACED, betPlacementStatus);
		
		Assert.assertTrue(userBet.getMongoId() != null);
		System.out.println(userBet.getMongoId());
		
		user = mHelper.getUser(user.getMongoId());
		Assert.assertEquals(1, user.getUserBets().size());
		UserBet retrievedBet = user.getUserBets().get(0);
		Assert.assertEquals(userBet.getMongoId(), retrievedBet.getMongoId());
		Assert.assertEquals(30d, userBet.getBetAmount(), 0d);
		Assert.assertEquals(BetStatus.PENDING, retrievedBet.getBetStatus());
	
		mHelper.deleteUser(user.getMongoId());
		user = mHelper.getUser(user.getMongoId());
		Assert.assertNull(user);
		
	}
	
	
	/**
	 * Creates a new user.
	 * Places a bet for the new user.
	 * Reads the user again to verify bet fields.
	 * Deletes the user.
	 * Verifies deletion.
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetLost(){
		
		User user = createUser();
		
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(1);
		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
		predCorrect.setOddValue(3.5d);
		preds.add(predCorrect);
		
		UserPrediction predWrong = new UserPrediction();
		predWrong.setEventId(2);
		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predWrong.setPredictionType(PredictionType.AWAY_WIN);
		predWrong.setOddValue(2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		Set<MatchEvent> events = new HashSet<>();
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(1);
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		events.add(eventWon);
		MatchEvent eventLost = new MatchEvent();
		eventLost.setId(2);
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.DRAW.getCode());
		events.add(eventLost);
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				mongoClientHelperImpl.settlePredictions(session, events);
			}
		}.execute();
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		user = mHelper.getUser(user.getMongoId());
		
		Assert.assertEquals(new Integer(1), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(1), user.getOverallLostSlipsCount());
		
		Assert.assertEquals(new Integer(1), user.getMonthlyLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyLostSlipsCount());
		
		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
		Assert.assertEquals(expectedBalance, user.getBalance());
		
		mHelper.deleteUser(user.getMongoId());
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);
		
	}

	/**
	 * Creates a new user.
	 * Places a bet with two predictions for the new user.
	 * Settles the predictions in two steps.
	 * One event is settled in every step.
	 * The first prediction is lost and the second is won.
	 * The bet should be lost.
	 * Deletes the user.
	 * Verifies deletion.
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetLostInMultipleSteps(){
		
		User user = createUser();
		
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(1);
		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
		predCorrect.setOddValue(3.5d);
		preds.add(predCorrect);
		
		UserPrediction predWrong = new UserPrediction();
		predWrong.setEventId(2);
		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predWrong.setPredictionType(PredictionType.AWAY_WIN);
		predWrong.setOddValue(2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		Set<MatchEvent> events = new HashSet<>();
		
		MatchEvent eventLost = new MatchEvent();
		eventLost.setId(2);
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.DRAW.getCode());
		events.add(eventLost);
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				mongoClientHelperImpl.settlePredictions(session, events);
			}
		}.execute();
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		User userAfterFirstStep = mHelper.getUser(user.getMongoId());
		
		Assert.assertEquals(new Integer(1), userAfterFirstStep.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(0), userAfterFirstStep.getOverallWonEventsCount());
		Assert.assertEquals(BetStatus.PENDING_LOST.getCode(), userAfterFirstStep.getUserBets().get(0).getBetStatus().getCode());
		
		
		//second step
		
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(1);
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		events.add(eventWon);
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				mongoClientHelperImpl.settlePredictions(session, events);
			}
		}.execute();
		
		
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		user = mHelper.getUser(user.getMongoId());
		Assert.assertEquals(BetStatus.SETTLED_UNFAVOURABLY.getCode(), user.getUserBets().get(0).getBetStatus().getCode());
		
		Assert.assertEquals(new Integer(1), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(1), user.getOverallLostSlipsCount());
		
		Assert.assertEquals(new Integer(1), user.getMonthlyLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyLostSlipsCount());
		
		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
		Assert.assertEquals(expectedBalance, user.getBalance());
		
		mHelper.deleteUser(user.getMongoId());
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);
		
	}

	/**
	 * Creates a new user.
	 * Places a bet with two predictions for the new user.
	 * Settles the same prediction twice.
	 * The prediction is won.
	 * The bet should be pending.
	 * Deletes the user.
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetLostInMultipleStepsStillPending(){
		
		User user = createUser();
		
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(1);
		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
		predCorrect.setOddValue(3.5d);
		preds.add(predCorrect);
		
		UserPrediction predWrong = new UserPrediction();
		predWrong.setEventId(2);
		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predWrong.setPredictionType(PredictionType.AWAY_WIN);
		predWrong.setOddValue(2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		Set<MatchEvent> events = new HashSet<>();
		
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(1);
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		events.add(eventWon);
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				mongoClientHelperImpl.settlePredictions(session, events);
			}
		}.execute();
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		User userAfterFirstStep = mHelper.getUser(user.getMongoId());
		
		Assert.assertEquals(new Integer(0), userAfterFirstStep.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(1), userAfterFirstStep.getOverallWonEventsCount());
		Assert.assertEquals(BetStatus.PENDING.getCode(), userAfterFirstStep.getUserBets().get(0).getBetStatus().getCode());
		
		
		//second step
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		user = mHelper.getUser(user.getMongoId());
		Assert.assertEquals(BetStatus.PENDING.getCode(), user.getUserBets().get(0).getBetStatus().getCode());
		
		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getOverallLostSlipsCount());
		
		Assert.assertEquals(new Integer(0), user.getMonthlyLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getMonthlyLostSlipsCount());
		
		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
		Assert.assertEquals(expectedBalance, user.getBalance());
		
		mHelper.deleteUser(user.getMongoId());
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);
		
	}

	
	/**
	 * Creates a new user.
	 * Places a bet for the new user.
	 * Reads the user again to verify bet fields.
	 * Deletes the user.
	 * Verifies deletion.
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetWon(){
		
		User user = createUser();
		
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(1);
		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
		predCorrect.setOddValue(3.5d);
		preds.add(predCorrect);
		
		UserPrediction predWrong = new UserPrediction();
		predWrong.setEventId(2);
		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predWrong.setPredictionType(PredictionType.AWAY_WIN);
		predWrong.setOddValue(2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		user = mHelper.getUser(user.getMongoId());
		
		Double newBalance = 970d;
		Assert.assertEquals(newBalance, user.getBalance());
		
		Set<MatchEvent> events = new HashSet<>();
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(1);
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		events.add(eventWon);
		MatchEvent eventLost = new MatchEvent();
		eventLost.setId(2);
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.AWAY_WIN.getCode());
		events.add(eventLost);
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				mongoClientHelperImpl.settlePredictions(session, events);
			}
		}.execute();
		
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(CollectionNames.USER_BETS);
		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(CollectionNames.USERS);
		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(CollectionNames.BET_PREDICTIONS);

		    	
		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		    	FindIterable<Document> find = betsCollection.find(betFilter);
				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
			}
		}.execute();
		
		mHelper.deleteSettledEvent(1);
		mHelper.deleteSettledEvent(2);

		user = mHelper.getUser(user.getMongoId());
		
		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(2), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getOverallLostSlipsCount());
		
		Assert.assertEquals(new Integer(0), user.getMonthlyLostEventsCount());
		Assert.assertEquals(new Integer(2), user.getMonthlyWonEventsCount());
		Assert.assertEquals(new Integer(1), user.getMonthlyWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getMonthlyLostSlipsCount());
		
		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount() + userBet.getPossibleEarnings();
		Assert.assertEquals( expectedBalance, user.getBalance());
		
		mHelper.deleteUser(user.getMongoId());
		
	}
	
	
	//@Test
	public void testGetStatsAndIncidents(){
		try {
			MatchEventIncidents incidents = SportScoreClient.getIncidents(2070730);
			
			MatchEvent e = new MatchEvent();
			e.setId(2070730);
			e.setIncidents(incidents);
			
//			MatchEventStatistics statistics = SportScoreClient.getStatistics(2070730);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	
//	@Test
//	public void testISO() throws ParseException{
//		
//		
//		Document search = new Document("match_id", "408642");
//		Executor<Event> betsExecutor = new Executor<Event>(new TypeToken<Event>() { });
//		List<Event> bets = MongoCollectionUtils.get(CollectionNames.EVENTS, search, betsExecutor);
//		Event event = bets.get(0);
//		String inpt = event.getMatchDate()+ " "+event.getMatchTime();
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);
//
//	    ZonedDateTime madridTime = LocalDateTime.parse(inpt, dtf)
//	            .atOffset(ZoneOffset.UTC)
//	            .atZoneSameInstant(ZoneId.systemDefault());
//		
//	    System.out.println(madridTime.toString());
//	    
//		
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);
//		System.out.println(simpleDateFormat.format(new Date(bets.get(0).getMatchFullDate().get$numberLong())));
//	}
	
	User createUser() {
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		User user = new User();
		user.setUsername("TestUser"+System.currentTimeMillis());
		user.setPassword("pass");
		user.setEmail(System.currentTimeMillis() + "test@test.gr");
		user = mHelper.createUser(user );
		return user;
	}
	
	MatchEvent createEvent(Integer eventId, MatchEventStatus status) {
		MatchEvent event = new MatchEvent();
		event.setId(eventId);
		event.setStatus(status.getStatusStr());
		event.setHome_team(createTeam(1, "homeTeam"));
		event.setAway_team(createTeam(2, "awayTeam"));
		return event;
	}


	Team createTeam(int teamId, String teamName) {
		Team team = new Team();
		team.setId(teamId);
		team.setName(teamName);
		return team;
	}
	
}
