package gr.server.impl.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import gr.server.bets.settle.impl.UserBetHandler;
import gr.server.bets.settle.impl.UserBetPredictionHandler;
import gr.server.bets.settle.impl.UserBetWithdrawnPredictionHandler;
import gr.server.common.MongoCollectionConstants;
import gr.server.common.ServerConstants;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.bet.enums.PredictionType;
import gr.server.data.constants.MongoFields;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserMonthlyBalance;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class TestMongoClientHelper {

	final double odd_value = 3.5d;
	
	Team homeTeam;

	Team awayTeam;

	@Before
	public void setup() {
		homeTeam = new Team();
		homeTeam.setId(1);
		awayTeam = new Team();
		awayTeam.setId(2);

//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
	}

	@After
	public void clear() {
		FootballApiCache.ALL_EVENTS.clear();
	}

	
//	/**
//	 * Creates a new user.
//	 * Places a bet for the new user.
//	 * Reads the user again to verify bet fields.
//	 * Deletes the user.
//	 * Verifies deletion.
//	 */
//	@Test
//	public void testPlaceBet_success(){
//
//		User user = createUser();
//		
//		Integer matchEventId = 395975;
//		FootballApiCache.ALL_EVENTS.put(matchEventId, createEvent(matchEventId, MatchEventStatus.NOTSTARTED));
//		
//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//		
//		UserBet userBet = new UserBet();
//		userBet.setMongoUserId(user.getMongoId());
//		userBet.setBetAmount(30);
//		
//		List<UserPrediction> preds = new ArrayList<UserPrediction>();
//		UserPrediction pred = new UserPrediction();
//		pred.setEventId(matchEventId);
//		pred.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		pred.setPredictionType(PredictionType.AWAY_WIN);
//		pred.setOddValue(13.5d);
//		preds.add(pred);
//		
//		
//		userBet.setPredictions(preds);
//		BetPlacementStatus betPlacementStatus = mHelper.placeBet(userBet);
//		Assert.assertEquals(BetPlacementStatus.PLACED, betPlacementStatus);
//		
//		Assert.assertTrue(userBet.getMongoId() != null);
//		System.out.println(userBet.getMongoId());
//		
//		user = mHelper.getUser(user.getMongoId());
//		Assert.assertEquals(1, user.getUserBets().size());
//		UserBet retrievedBet = user.getUserBets().get(0);
//		Assert.assertEquals(userBet.getMongoId(), retrievedBet.getMongoId());
//		Assert.assertEquals(30d, userBet.getBetAmount(), 0d);
//		Assert.assertEquals(BetStatus.PENDING, retrievedBet.getBetStatus());
//	
//		mHelper.deleteUser(user.getMongoId());
//		user = mHelper.getUser(user.getMongoId());
//		Assert.assertNull(user);
//		
//	}
//	
//	
//	/**
//	 * Creates a new user.
//	 * Places a bet for the new user.
//	 * Reads the user again to verify bet fields.
//	 * Deletes the user.
//	 * Verifies deletion.
//	 * @throws Exception 
//	 */
//	@SuppressWarnings("removal")
//	@Test
//	public void testSettleBetLost() throws Exception{
//		
//		
//		Set<MatchEvent> events = new HashSet<>();
//		MatchEvent eventWon = new MatchEvent();
//		eventWon.setHome_team(homeTeam);
//		eventWon.setAway_team(awayTeam);
//		eventWon.setId(1);
//		eventWon.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventWon);
//		MatchEvent eventLost = new MatchEvent();
//		eventLost.setId(2);
//		eventLost.setHome_team(homeTeam);
//		eventLost.setAway_team(awayTeam);
//		eventLost.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventLost);
//		events.forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));
//		
//		
//		User user = createUser();
//		
//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//
//		
//		UserBet userBet = new UserBet();
//		userBet.setMongoUserId(user.getMongoId());
//		userBet.setBetAmount(30);
//		
//		List<UserPrediction> preds = new ArrayList<UserPrediction>();
//		
//		UserPrediction predCorrect = new UserPrediction();
//		predCorrect.setEventId(1);
//		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
//		predCorrect.setOddValue(3.5d);
//		preds.add(predCorrect);
//		
//		UserPrediction predWrong = new UserPrediction();
//		predWrong.setEventId(2);
//		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predWrong.setPredictionType(PredictionType.AWAY_WIN);
//		predWrong.setOddValue(2.5d);
//		preds.add(predWrong);
//		
//		userBet.setPredictions(preds);
//		mHelper.placeBet(userBet);
//		
//		
//		
//		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
//		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
//		
//		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
//		eventLost.setWinner_code(PredictionType.DRAW.getCode());
//		
//			
//		MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//		mongoClientHelperImpl.settlePredictions(events);
//
//		
//		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
//		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.BET_PREDICTIONS);
//		
//		
//		Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
//
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//		    	FindIterable<Document> find = betsCollection.find(session, betFilter);
//				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
//			}
//		}.execute();
//		
//		user = mHelper.getUser(user.getMongoId());
//		
//		
//		mHelper.deleteUser(user.getMongoId());
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
//		
//		Assert.assertEquals(new Integer(1), user.getOverallLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
//		Assert.assertEquals(new Integer(1), user.getOverallLostSlipsCount());
//		
//		Assert.assertEquals(new Integer(1), user.getMonthlyLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
//		Assert.assertEquals(new Integer(1), user.getMonthlyLostSlipsCount());
//		
//		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
//		Assert.assertEquals(expectedBalance, user.getBalance());
//		
//		
//		
//	}
//
//	/**
//	 * Creates a new user.
//	 * Places a bet with two predictions for the new user.
//	 * Settles the predictions in two steps.
//	 * One event is settled in every step.
//	 * The first prediction is lost and the second is won.
//	 * The bet should be lost.
//	 * Deletes the user.
//	 * Verifies deletion.
//	 * @throws Exception 
//	 */
//	@SuppressWarnings("removal")
//	@Test
//	public void testSettleBetLostInMultipleSteps() throws Exception{
//
//		Set<MatchEvent> events = new HashSet<>();
//		
//		MatchEvent eventWon = new MatchEvent();
//		eventWon.setId(1);
//
//		eventWon.setHome_team(homeTeam);
//		eventWon.setAway_team(awayTeam);
//		eventWon.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventWon);
//		
//		MatchEvent eventLost = new MatchEvent();
//		eventLost.setId(2);
//		eventLost.setHome_team(homeTeam);
//		eventLost.setAway_team(awayTeam);
//		eventLost.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventLost);
//		
//		events.forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));	
//		
//		User user = createUser();
//		
//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
//		
//		UserBet userBet = new UserBet();
//		userBet.setMongoUserId(user.getMongoId());
//		userBet.setBetAmount(30);
//		
//		List<UserPrediction> preds = new ArrayList<UserPrediction>();
//		
//		UserPrediction predCorrect = new UserPrediction();
//		predCorrect.setEventId(1);
//		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
//		predCorrect.setOddValue(3.5d);
//		preds.add(predCorrect);
//		
//		UserPrediction predWrong = new UserPrediction();
//		predWrong.setEventId(2);
//		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predWrong.setPredictionType(PredictionType.AWAY_WIN);
//		predWrong.setOddValue(2.5d);
//		preds.add(predWrong);
//		
//		userBet.setPredictions(preds);
//		mHelper.placeBet(userBet);
//		
//		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
//		eventLost.setWinner_code(PredictionType.DRAW.getCode());
//
//
//		MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//		mongoClientHelperImpl.settlePredictions(Set.of(eventLost));
//
//		
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
//		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.BET_PREDICTIONS);
//
//		    	
//		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
//		    	FindIterable<Document> find = betsCollection.find(betFilter);
//				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
//			}
//		}.execute();
//		
//		User userAfterFirstStep = mHelper.getUser(user.getMongoId());
//		
//		Assert.assertEquals(new Integer(1), userAfterFirstStep.getOverallLostEventsCount());
//		Assert.assertEquals(new Integer(0), userAfterFirstStep.getOverallWonEventsCount());
//		Assert.assertEquals(BetStatus.PENDING_LOST.getCode(), userAfterFirstStep.getUserBets().get(0).getBetStatus().getCode());
//		
//		
//		//second step
//		
//		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
//		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
//		
//		
////		new MongoTransactionalBlock() {
////			@Override
////			public void begin() throws Exception {
////				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//				mongoClientHelperImpl.settlePredictions(events);
////			}
////		}.execute();
//		
//		
//		
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
//		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.BET_PREDICTIONS);
//
//		    	
//		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
//		    	FindIterable<Document> find = betsCollection.find(betFilter);
//				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
//			}
//		}.execute();
//		
//		user = mHelper.getUser(user.getMongoId());
//		
//		mHelper.deleteUser(user.getMongoId());
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
//		
//		Assert.assertEquals(BetStatus.SETTLED_UNFAVOURABLY.getCode(), user.getUserBets().get(0).getBetStatus().getCode());
//		
//		Assert.assertEquals(new Integer(1), user.getOverallLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
//		Assert.assertEquals(new Integer(1), user.getOverallLostSlipsCount());
//		
//		Assert.assertEquals(new Integer(1), user.getMonthlyLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
//		Assert.assertEquals(new Integer(1), user.getMonthlyLostSlipsCount());
//		
//		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
//		Assert.assertEquals(expectedBalance, user.getBalance());
//		
//		
//		
//	}
//
//	/**
//	 * Creates a new user.
//	 * Places a bet with two predictions for the new user.
//	 * Settles the same prediction twice.
//	 * The prediction is won.
//	 * The bet should be pending.
//	 * Deletes the user.
//	 * @throws Exception 
//	 */
//	@SuppressWarnings("removal")
//	@Test
//	public void testSettleBetLostInMultipleStepsStillPending() throws Exception{
//		
//		Set<MatchEvent> events = new HashSet<>();
//		
//		MatchEvent eventWon = new MatchEvent();
//		eventWon.setId(1);
//		eventWon.setHome_team(homeTeam);
//		eventWon.setAway_team(awayTeam);
//		eventWon.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventWon);
//		
//		MatchEvent eventLost = new MatchEvent();
//		eventLost.setId(2);
//		eventLost.setHome_team(homeTeam);
//		eventLost.setAway_team(awayTeam);
//		eventLost.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
//		events.add(eventLost);
//		
//		events.forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));	
//		
//		User user = createUser();
//		
//		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
//		
//		UserBet userBet = new UserBet();
//		userBet.setMongoUserId(user.getMongoId());
//		userBet.setBetAmount(30);
//		
//		List<UserPrediction> preds = new ArrayList<UserPrediction>();
//		
//		UserPrediction predCorrect = new UserPrediction();
//		predCorrect.setEventId(1);
//		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predCorrect.setPredictionType(PredictionType.AWAY_WIN);
//		predCorrect.setOddValue(3.5d);
//		preds.add(predCorrect);
//		
//		UserPrediction predWrong = new UserPrediction();
//		predWrong.setEventId(2);
//		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
//		predWrong.setPredictionType(PredictionType.AWAY_WIN);
//		predWrong.setOddValue(2.5d);
//		preds.add(predWrong);
//		
//		userBet.setPredictions(preds);
//		mHelper.placeBet(userBet);
//		
//		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
//		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
//		
//		
//
//		MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//		mongoClientHelperImpl.settlePredictions(events);
//
//		
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
//		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.BET_PREDICTIONS);
//
//		    	
//		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
//		    	FindIterable<Document> find = betsCollection.find(betFilter);
//				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
//			}
//		}.execute();
//		
//		User userAfterFirstStep = mHelper.getUser(user.getMongoId());
//		
//		Assert.assertEquals(new Integer(0), userAfterFirstStep.getOverallLostEventsCount());
//		Assert.assertEquals(new Integer(1), userAfterFirstStep.getOverallWonEventsCount());
//		Assert.assertEquals(BetStatus.PENDING.getCode(), userAfterFirstStep.getUserBets().get(0).getBetStatus().getCode());
//		
//		
//		//second step
//		
//		new MongoTransactionalBlock() {
//			@Override
//			public void begin() throws Exception {
//				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
//				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
//		    	MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		    	MongoCollection<Document> predictionsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.BET_PREDICTIONS);
//
//		    	
//		    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
//		    	FindIterable<Document> find = betsCollection.find(betFilter);
//				mongoClientHelperImpl.settleOpenBet(session, find.first(), usersCollection, predictionsCollection, betsCollection);
//			}
//		}.execute();
//		
//		user = mHelper.getUser(user.getMongoId());
//		mHelper.deleteUser(user.getMongoId());
//		mHelper.deleteSettledEvent(1);
//		mHelper.deleteSettledEvent(2);
//		
//		Assert.assertEquals(BetStatus.PENDING.getCode(), user.getUserBets().get(0).getBetStatus().getCode());
//		
//		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getOverallWonSlipsCount());
//		Assert.assertEquals(new Integer(0), user.getOverallLostSlipsCount());
//		
//		Assert.assertEquals(new Integer(0), user.getMonthlyLostEventsCount());
//		Assert.assertEquals(new Integer(1), user.getMonthlyWonEventsCount());
//		Assert.assertEquals(new Integer(0), user.getMonthlyWonSlipsCount());
//		Assert.assertEquals(new Integer(0), user.getMonthlyLostSlipsCount());
//		
//		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount();
//		Assert.assertEquals(expectedBalance, user.getBalance());
//		
//		
//		
//	}
//
//	
	/**
	 * Creates a new user. Places a bet for the new user. Reads the user again to
	 * verify bet fields. Deletes the user. Verifies deletion.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetWon() throws Exception {

		Set<MatchEvent> events = new HashSet<>();
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(1);
		eventWon.setStart_at(new SimpleDateFormat(ServerConstants.DATE_WITH_TIME_FORMAT).format(new Date()));

		eventWon.setHome_team(homeTeam);
		eventWon.setAway_team(awayTeam);
		eventWon.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
		events.add(eventWon);
		MatchEvent eventLost = new MatchEvent();
		eventLost.setId(2);
		eventLost.setHome_team(homeTeam);
		eventLost.setAway_team(awayTeam);
		eventLost.setStart_at(new SimpleDateFormat(ServerConstants.DATE_WITH_TIME_FORMAT).format(new Date()));

		eventLost.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());
		events.add(eventLost);
		events.forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));

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
		predCorrect.setOddValue(odd_value);
		preds.add(predCorrect);

		UserPrediction predWrong = new UserPrediction();
		predWrong.setEventId(2);
		predWrong.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predWrong.setPredictionType(PredictionType.AWAY_WIN);
		predWrong.setOddValue(2.5d);
		preds.add(predWrong);

		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);

		user = mHelper.getUser(user.getMongoId(), true, true, true, true);

		Double newBalance = 970d;
		
		Assert.assertEquals(newBalance, Double.valueOf(user.currentMonthBalance()));

		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		eventLost.setWinner_code(PredictionType.AWAY_WIN.getCode());

		MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
		mongoClientHelperImpl.settlePredictions(Set.of(eventLost, eventWon));

		boolean settled = new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
				MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
				MongoCollection<Document> predictionsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);

				Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
				FindIterable<Document> find = betsCollection.find(betFilter);
				Set<Document> betsDocs = new HashSet<>();
				for (Document d : find)
				betsDocs.add(d);
				
				mongoClientHelperImpl.settleOpenBets(betsDocs);
			}
		}.execute();
		
		Assert.assertTrue(settled);

		user = mHelper.getUser(user.getMongoId(), true, true, true, true);
		mHelper.deleteUser(user.getMongoId());

		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(2), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getOverallLostSlipsCount());

		UserMonthlyBalance currentBalanceObject = user.currentBalanceObject();
		Assert.assertEquals(0, currentBalanceObject.getMonthlyLostEventsCount());
		Assert.assertEquals(2, currentBalanceObject.getMonthlyWonEventsCount());
		Assert.assertEquals(1, currentBalanceObject.getMonthlyWonSlipsCount());
		Assert.assertEquals(0, currentBalanceObject.getMonthlyLostSlipsCount());

		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount()
				+ userBet.getPossibleEarnings();
		Assert.assertEquals(expectedBalance, new Double( user.currentMonthBalance()));

	}
	
	
	/**
	 * Creates a new user. Places a bet for the new user. Reads the user again to
	 * verify bet fields. Deletes the user. Verifies deletion.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSettleMultipleBets() throws Exception {

		User user = createUser();
		System.out.println("USERID IS :::::" +user.getMongoId());
		
		createEvent(10);
		
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.AWAY_WIN);

		settleEvent(10, MatchEventStatus.FINISHED, PredictionType.AWAY_WIN);


		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		user = mHelper.getUser(user.getMongoId(), true, true, true, true);

		Double newBalance = 1000d - (24 * 30);
		
		Assert.assertEquals(newBalance, Double.valueOf(user.currentMonthBalance()));
		

		System.out.println("BALANCE IS " + newBalance);
		
		settlePredictionsInThreads();
		settleBetsInThreads(user.getMongoId());
		
		
		Thread.sleep(20000);
		
		user = mHelper.getUser(user.getMongoId(), true, true, true, true);

		newBalance = newBalance + (24 * 30 * odd_value);// odd 
		
		Assert.assertEquals(newBalance, Double.valueOf(user.currentMonthBalance()));
		
		
		mHelper.deleteUser(user.getMongoId());
		
		


	}
	
	
	@Test
	public void testSections() {
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				// TODO Auto-generated method stub
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				List<Section> sectionsFromDb = mongoClientHelperImpl.getSectionsFromDb(session);
				mongoClientHelperImpl.updateSections(sectionsFromDb);
				
				
				List<Section> incomingSections = MockApiClient.getSectionsFromFile().getData();
				mongoClientHelperImpl.updateSections(incomingSections);
				
				sectionsFromDb = mongoClientHelperImpl.getSectionsFromDb(session);
				System.out.println(sectionsFromDb.get(0).getName_translations().get("en"));
				
			}
		};
		
	}
	
	@Test
	public void testLeagues() {
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				// TODO Auto-generated method stub
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				List<League> sectionsFromDb = mongoClientHelperImpl.getLeaguesFromDb(session);
				mongoClientHelperImpl.updateLeagues(sectionsFromDb);
				
				
				List<League> incomingSections = MockApiClient.getLeaguesFromFile().getData();
				mongoClientHelperImpl.updateLeagues(incomingSections);
				
				sectionsFromDb = mongoClientHelperImpl.getLeaguesFromDb(session);
				System.out.println(sectionsFromDb.get(0).getName_translations().get("en"));
				
			}
		};
		
	}

	@Test
	public void testSettleInThreadss() throws InterruptedException {

		User createUser = createUser();
		createEvent(1);
		placeBetForEvent(1, createUser.getMongoId(), PredictionType.AWAY_WIN);
		settleEvent(1, MatchEventStatus.FINISHED, PredictionType.AWAY_WIN);

		
		settlePredictionsInThreads();
		settleWithdrawnPredictionsInThreads();
		settleBetsInThreads(createUser.getMongoId());

		Thread.sleep(10000);
		
		createUser = new MongoClientHelperImpl().getUser(createUser.getMongoId(), true, false, true, false);

		Assert.assertEquals(ServerConstants.STARTING_BALANCE, new Double(createUser.getBalance()));
		
		UserBet userBet = createUser.getUserBets().get(0);
		
		Assert.assertEquals(BetStatus.SETTLED_WITHDRAWN.getCode(), userBet.getBetStatus());
		
		UserPrediction userPrediction = userBet.getPredictions().get(0);
		Assert.assertEquals(PredictionStatus.WITHDRAWN.getCode(), userPrediction.getPredictionStatus().getCode());
		
		new MongoClientHelperImpl().deleteUser(createUser.getMongoId());
	}
	
	private void settleWithdrawnPredictionsInThreads() {
/** withdrawn preds **/
		
		
		ExecutorService subscribersWithDrawnPredictionSettling = Executors.newFixedThreadPool(UserBetWithdrawnPredictionHandler.NUM_WORKERS);
		
		Runnable settleWithdrawnPredsRunnableOrchestrator = () -> { 
			
			try {
				subscribersWithDrawnPredictionSettling.submit(new UserBetWithdrawnPredictionHandler(FootballApiCache.WITHDRAWN_EVENTS.size()));
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settleWithdrawnPredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settleWithdrawnPredsRunnableOrchestratorTask.scheduleAtFixedRate(settleWithdrawnPredsRunnableOrchestrator, 0, 10, TimeUnit.SECONDS);
		
	}

	private void settlePredictionsInThreads() {
/** new preds **/
		
		
		ExecutorService subscribersPredictionSettling = Executors.newFixedThreadPool(UserBetPredictionHandler.NUM_WORKERS);
		
		Runnable settlePredsRunnableOrchestrator = () -> { 
			
			try {
					int batch = (FootballApiCache.FINISHED_EVENTS.size() / UserBetPredictionHandler.NUM_WORKERS) > 0 ?
							FootballApiCache.FINISHED_EVENTS.size() / UserBetPredictionHandler.NUM_WORKERS 
							: FootballApiCache.FINISHED_EVENTS.size() % UserBetPredictionHandler.NUM_WORKERS;
				
					for (int i = 0; i < UserBetPredictionHandler.NUM_WORKERS; i++) {
			            subscribersPredictionSettling.submit(new UserBetPredictionHandler(batch));
			        }
			
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
		
		ScheduledExecutorService settlePredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settlePredsRunnableOrchestratorTask.scheduleAtFixedRate(settlePredsRunnableOrchestrator, 3, 13, TimeUnit.SECONDS);
		
	}

	private void settleBetsInThreads(String mongoId) {
		// bets
		
		ExecutorService subscribersBetSettling = Executors.newFixedThreadPool(1);// (UserBetHandler.NUM_WORKERS);
		
		Runnable settleBetsRunnableOrchestrator = () -> { 
		
			try {
				
				 MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				 Bson pendingOrPendingLostBetsFilter = mongoClientHelperImpl.pendingOrPendingLostBetsFilter();
				 Bson pendingOfUserFilter = Filters.eq(MongoFields.MONGO_USER_ID, mongoId);

				 Bson finalFilter = Filters.and(pendingOrPendingLostBetsFilter, pendingOfUserFilter);
				 
				 long allUnsettledBetsSize = mongoClientHelperImpl.fetchFilterSize(MongoCollectionConstants.USER_BETS, finalFilter);
				System.out.println("ALL UNSETTLED BETS:::" +allUnsettledBetsSize);
				if (allUnsettledBetsSize == 0) {
					System.out.println("NOTHING TO SETTLE");
					return;
				}
				
				long batchSize = (allUnsettledBetsSize / UserBetHandler.NUM_WORKERS) > 0 ?
						allUnsettledBetsSize / UserBetHandler.NUM_WORKERS 
						: allUnsettledBetsSize % UserBetHandler.NUM_WORKERS;
				
				FindIterable<Document> iterable = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS).find(pendingOrPendingLostBetsFilter).batchSize((int) batchSize);
			
				List<Document> userBetsDocument = new ArrayList<>();
				for (Document bet : iterable) {
					userBetsDocument.add(bet);
					
					if (userBetsDocument.size() == batchSize) {
						subscribersBetSettling.submit(new UserBetHandler(new HashSet<>(userBetsDocument)));
						userBetsDocument.clear();
					}
					
		        }
				
				if (!userBetsDocument.isEmpty()) {
					subscribersBetSettling.submit(new UserBetHandler(new HashSet<>(userBetsDocument)));
				}
			
				}catch (Exception e) {
					e.printStackTrace();
				}
			};
	
		ScheduledExecutorService settleBetsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settleBetsRunnableOrchestratorTask.scheduleAtFixedRate(settleBetsRunnableOrchestrator, 5, 15, TimeUnit.SECONDS);
		
	}

	@Test
	public void testAwardWinner() {
		
//		MongoCollection<Document> usersCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USERS);
//		
//		new Random().nextDouble(10000);
//		
//		Document validDoc = new Document(MongoFields.USER_BALANCE, new Random().nextDouble(10000));
//		Document pushDocument = new Document("$set", validDoc);
//		
//		usersCollection.updateMany(
//				
//                new Document(), 
//                pushDocument  
//            );
//		
		
		new MongoClientHelperImpl().closeMonthlyBalancesAndComputeMonthWinner();
	}
	
//	
//	
//	//@Test
//	public void testGetStatsAndIncidents(){
//		try {
//			MatchEventIncidents incidents = SportScoreClient.getIncidents(2070730);
//			
//			MatchEvent e = new MatchEvent();
//			e.setId(2070730);
////			e.setIncidents(incidents);
//			
////			MatchEventStatistics statistics = SportScoreClient.getStatistics(2070730);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
////	
////	@Test
////	public void testISO() throws ParseException{
////		
////		
////		Document search = new Document("match_id", "408642");
////		Executor<Event> betsExecutor = new Executor<Event>(new TypeToken<Event>() { });
////		List<Event> bets = MongoCollectionUtils.get(MongoCollectionConstants.EVENTS, search, betsExecutor);
////		Event event = bets.get(0);
////		String inpt = event.getMatchDate()+ " "+event.getMatchTime();
////		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);
////
////	    ZonedDateTime madridTime = LocalDateTime.parse(inpt, dtf)
////	            .atOffset(ZoneOffset.UTC)
////	            .atZoneSameInstant(ZoneId.systemDefault());
////		
////	    System.out.println(madridTime.toString());
////	    
////		
////		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);
////		System.out.println(simpleDateFormat.format(new Date(bets.get(0).getMatchFullDate().get$numberLong())));
////	}
//	

	void createEvent(int eventId) throws InterruptedException {
		MatchEvent eventWon = new MatchEvent();
		eventWon.setId(eventId);
		eventWon.setHome_team(homeTeam);
		eventWon.setAway_team(awayTeam);
		eventWon.setStart_at(new SimpleDateFormat(ServerConstants.DATE_WITH_TIME_FORMAT).format(new Date()));
		eventWon.setStatus(MatchEventStatus.NOTSTARTED.getStatusStr());

		FootballApiCache.ALL_EVENTS.put(eventWon.getId(), eventWon);
		
		
	}

	private void settleEvent(int eventId, MatchEventStatus settle, PredictionType predictionType) throws InterruptedException {
		MatchEvent eventWon = FootballApiCache.ALL_EVENTS.get(eventId);
		eventWon.setStatus(settle.getStatusStr());
		eventWon.setWinner_code(predictionType.getCode());

		
		if (MatchEventStatus.POSTPONED == settle) {
			FootballApiCache.WITHDRAWN_EVENTS.put(eventWon);
		}else {
			FootballApiCache.FINISHED_EVENTS.put(eventWon);			
		}

	
	}

	private void placeBetForEvent(Integer eventId, String mongoId, PredictionType predictionType) {

		UserBet userBet = new UserBet();
		userBet.setMongoUserId(mongoId);
		userBet.setBetAmount(30);

		List<UserPrediction> preds = new ArrayList<UserPrediction>();

		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(eventId);
		predCorrect.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		predCorrect.setPredictionType(predictionType);
		predCorrect.setOddValue(3.5d);
		preds.add(predCorrect);

		userBet.setPredictions(preds);

		new MongoClientHelperImpl().placeBet(userBet);
	
	}

	User createUser() {
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		User user = new User();
		user.setUsername("TestUser" + System.currentTimeMillis());
		user.setPassword("pass");
		user.setEmail(System.currentTimeMillis() + "test@test.gr");
		user = mHelper.createUser(user);
		return mHelper.getUser(user.getMongoId(), true, true, true, true);
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
