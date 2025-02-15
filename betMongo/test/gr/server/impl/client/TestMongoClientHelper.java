package gr.server.impl.client;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import com.mongodb.client.model.Updates;

import gr.server.bets.settle.impl.UserBetHandler;
import gr.server.bets.settle.impl.UserBetPredictionHandler;
import gr.server.bets.settle.impl.UserBetWithdrawnPredictionHandler;
import gr.server.common.MongoCollectionConstants;
import gr.server.common.ServerConstants;
import gr.server.common.enums.PurchasedProduct;
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
import gr.server.data.user.model.objects.UserPurchase;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.ExecutorsBetHelper;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class TestMongoClientHelper {

	final double odd_value = 3.5d;

	Team homeTeam;

	Team awayTeam;

	List<User> users = new ArrayList<>();

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
		MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
		for (User user : users) {
//			mongoClientHelperImpl.deleteUser(user.getMongoId());

		}
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
		users.add(user);

		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(800);

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

		long threeDaysBefore = 3 * 1000 * 24 * 60 * 60;
		user = mHelper.getUser(user.getMongoId(), 10, threeDaysBefore, true, true, true, true);

		Double newBalance = 200d;

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));

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
				MongoCollection<Document> betsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BETS);
				MongoCollection<Document> usersCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USERS);
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

		user = mHelper.getUser(user.getMongoId(), 10, threeDaysBefore, true, true, true, true);

		mHelper.deleteUser(user.getMongoId());

		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(2), user.getOverallWonEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonSlipsCount());
		Assert.assertEquals(new Integer(0), user.getOverallLostSlipsCount());

		UserMonthlyBalance currentBalanceObject = user.currentMonthBalance();
		Assert.assertEquals(0, currentBalanceObject.getMonthlyLostEventsCount());
		Assert.assertEquals(2, currentBalanceObject.getMonthlyWonEventsCount());
		Assert.assertEquals(1, currentBalanceObject.getMonthlyWonSlipsCount());
		Assert.assertEquals(0, currentBalanceObject.getMonthlyLostSlipsCount());

		Double expectedBalance = ServerConstants.STARTING_BALANCE - userBet.getBetAmount()
				+ userBet.getPossibleEarnings();
		Assert.assertEquals(expectedBalance, new Double(user.getBalance()));

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
		users.add(user);
		System.out.println("USERID IS :::::" + user.getMongoId());

		createEvent(10);

		placeBetForEvent(10, user.getMongoId(), PredictionType.DRAW);
		placeBetForEvent(10, user.getMongoId(), PredictionType.DRAW);
		placeBetForEvent(10, user.getMongoId(), PredictionType.DRAW);
		placeBetForEvent(10, user.getMongoId(), PredictionType.DRAW);
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
		placeBetForEvent(10, user.getMongoId(), PredictionType.HOME_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.HOME_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.HOME_WIN);
		placeBetForEvent(10, user.getMongoId(), PredictionType.HOME_WIN);

		settleEvent(10, MatchEventStatus.FINISHED, PredictionType.AWAY_WIN);

		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		long threeDaysBefore = 3 * 1000 * 24 * 60 * 60;

		user = mHelper.getUser(user.getMongoId(), 10, threeDaysBefore, true, true, true, true);

		Double newBalance = 1000d - (24 * 30);

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));

		System.out.println("BALANCE IS " + newBalance);

		settlePredictionsInThreads(1);
		settleBetsInThreads(4, user.getMongoId());

		Thread.sleep(20000);

		user = mHelper.getUser(user.getMongoId(), 10, threeDaysBefore, true, true, true, true);

		newBalance = newBalance + (16 * 30 * odd_value);// odd

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));

//		mHelper.deleteUser(user.getMongoId());

	}

	@Test
	public void deleteOrphanUsers() {

		new MongoTransactionalBlock<Void>() {

			@Override
			public void begin() throws Exception {
				// TODO Auto-generated method stub

				MongoCollection<Document> usersCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USERS);
				MongoCollection<Document> balancesCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_MONTHLY_BALANCE);
				FindIterable<Document> find = balancesCollection.find(session, new Document());
				Set<String> todel = new HashSet<>();
				for (Document document : find) {
					String mongoId = document.getString(MongoFields.MONGO_USER_ID);

					Bson filter = new Document(MongoFields.MONGO_ID, new ObjectId(mongoId));
					FindIterable<Document> find2 = usersCollection.find(filter);
					Document user = find2.first();
					if (user == null) {
						todel.add(mongoId);
						// System.out.println("MISSING:::" + mongoId);
					} else {
//						System.out.println("FOUND:::" + user.getString(MongoFields.USERNAME));
					}

				}
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				for (String string : todel) {
					mongoClientHelperImpl.deleteUser(string);
				}
			}
		}.execute();

	}

	@Test
	public void deleteOrphanBets() {

		new MongoTransactionalBlock<Void>() {

			@Override
			public void begin() throws Exception {
				// TODO Auto-generated method stub

				MongoCollection<Document> usersCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USERS);
				MongoCollection<Document> betsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BETS);
				MongoCollection<Document> predsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BETS);
				FindIterable<Document> find = betsCollection.find(session, new Document());
				Set<String> todel = new HashSet<>();
				for (Document document : find) {
					String mongoId = document.getString(MongoFields.MONGO_USER_ID);

					Bson filter = new Document(MongoFields.MONGO_ID, new ObjectId(mongoId));
					FindIterable<Document> find2 = usersCollection.find(filter);
					Document user = find2.first();
					if (user == null) {
//						todel.add(mongoId);
						betsCollection.deleteOne(document);
						System.out.println("DELETED:::" + document);
					} else {
//						System.out.println("FOUND:::" + user.getString(MongoFields.USERNAME));
					}

				}
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				for (String string : todel) {
//					mongoClientHelperImpl.deleteUser(string);
				}
			}
		}.execute();

	}

	@Test
	public void updateDelayedPredictions() throws InterruptedException {

		new ExecutorsBetHelper().scheduleSettleDelayedPredictions();

		Thread.sleep(30000);

	}

	@Test
	public void updateLeaders() throws InterruptedException {

		new ExecutorsBetHelper().scheduleFetchLeaderBoard(0, 1 * 100, TimeUnit.SECONDS);

		Thread.sleep(100000);

	}

	@Test
	public void updateOrphanBets() {

		new MongoTransactionalBlock<Void>() {

			@Override
			public void begin() throws Exception {
				// TODO Auto-generated method stub

				MongoCollection<Document> betsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BETS);
				MongoCollection<Document> predsCollection = MongoUtils
						.getMongoCollection(MongoCollectionConstants.USER_BET_PREDICTIONS);

				Bson pendingOrPendingLostBetsFilter = new MongoClientHelperImpl().pendingOrPendingLostBetsFilter();

				FindIterable<Document> find = betsCollection.find(session, pendingOrPendingLostBetsFilter);

				for (Document document : find) {
					String mongoBetId = document.getObjectId(MongoFields.MONGO_ID).toString();
					System.out.println("OPEN BET:::" + mongoBetId);

					Bson betIdFilter = Filters.eq(MongoFields.USER_BET_PREDICTION_BET_MONGO_ID, mongoBetId);

					FindIterable<Document> find2 = predsCollection.find(betIdFilter);
					for (Document pred : find2) {
						String mongoPredId = pred.getObjectId(MongoFields.MONGO_ID).toString();
						Bson filterL = Updates.set(MongoFields.USER_BET_PREDICTION_BET_LEAGUE_ID, 317);
						Bson filterE = Updates.set(MongoFields.EVENT_ID, 2493998);
						Bson filterD = Updates.set(MongoFields.USER_BET_PREDICTION_BET_START_AT, "2025-01-04 17:00:07");
						Bson combined = Updates.combine(filterD, filterL, filterE);

						Bson predIdFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(mongoPredId));

						predsCollection.updateOne(session, predIdFilter, combined);
						System.out.println("UPDATED PRED:::" + mongoPredId);
					}
				}

			}
		}.execute();

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

		settlePredictionsInThreads(UserBetPredictionHandler.NUM_WORKERS);
		settleWithdrawnPredictionsInThreads();
		settleBetsInThreads(UserBetHandler.NUM_WORKERS, createUser.getMongoId());

		Thread.sleep(10000);

		long threeDaysBefore = 3 * 1000 * 24 * 60 * 60;
		createUser = new MongoClientHelperImpl().getUser(createUser.getMongoId(), 10, threeDaysBefore, true, false,
				true, false);

		Assert.assertEquals(ServerConstants.STARTING_BALANCE, new Double(createUser.getBalance()));

		UserBet userBet = createUser.getUserBets().get(0);

		Assert.assertEquals(BetStatus.SETTLED_WITHDRAWN.getCode(), userBet.getBetStatus());

		UserPrediction userPrediction = userBet.getPredictions().get(0);
		Assert.assertEquals(PredictionStatus.WITHDRAWN.getCode(), userPrediction.getPredictionStatus().getCode());

		new MongoClientHelperImpl().deleteUser(createUser.getMongoId());
	}

	private void settleWithdrawnPredictionsInThreads() {
		/** withdrawn preds **/

		ExecutorService subscribersWithDrawnPredictionSettling = Executors
				.newFixedThreadPool(UserBetWithdrawnPredictionHandler.NUM_WORKERS);

		Runnable settleWithdrawnPredsRunnableOrchestrator = () -> {

			try {
				subscribersWithDrawnPredictionSettling
						.submit(new UserBetWithdrawnPredictionHandler(FootballApiCache.WITHDRAWN_EVENTS.size()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		ScheduledExecutorService settleWithdrawnPredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settleWithdrawnPredsRunnableOrchestratorTask.scheduleAtFixedRate(settleWithdrawnPredsRunnableOrchestrator, 0,
				10, TimeUnit.SECONDS);

	}

	private void settlePredictionsInThreads(int threadCount) {
		/** new preds **/

		ExecutorService subscribersPredictionSettling = Executors.newFixedThreadPool(threadCount);

		Runnable settlePredsRunnableOrchestrator = () -> {

			try {
				int batch = (FootballApiCache.FINISHED_EVENTS.size() / threadCount) > 0
						? FootballApiCache.FINISHED_EVENTS.size() / threadCount
						: FootballApiCache.FINISHED_EVENTS.size() % threadCount;

				for (int i = 0; i < threadCount; i++) {
					subscribersPredictionSettling.submit(new UserBetPredictionHandler(batch));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		ScheduledExecutorService settlePredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(1);
		settlePredsRunnableOrchestratorTask.scheduleAtFixedRate(settlePredsRunnableOrchestrator, 3, 13,
				TimeUnit.SECONDS);

	}
	
	@Test
	public void testSettleWithPurchase() throws InterruptedException {

		User createUser = createUser();
//		System.out.println(createUser.getMongoId());
//		if (2>1) return;
		UserPurchase bean = new UserPurchase();
		bean.setMongoUserId(createUser.getMongoId());
		bean.setPlatform("testplpl");
		bean.setProductId(PurchasedProduct.TOPUP_1000.getProductName());
		bean.setPurchaseToken("dummyToken");
		
		new MongoClientHelperImpl().storePurchase(bean);
		createUser = new MongoClientHelperImpl().getUser(createUser.getMongoId(), 0, 0, false, false,
				true, false);
		
		Assert.assertEquals(new Double(2000), createUser.getBalance());
		Assert.assertEquals(new Double(2000), createUser.currentMonthBalance().getBalance());
		Assert.assertEquals(new Double(1000), createUser.getRemainingCredits());
		
		
		createEvent(1);
		placeBetForEvent(1, createUser.getMongoId(), PredictionType.AWAY_WIN);
		
		createUser = new MongoClientHelperImpl().getUser(createUser.getMongoId(), 0, 0, false, false,
				true, false);
		
		System.out.println("created user " + createUser.getMongoId());
		
		Assert.assertEquals(new Double(1970d), createUser.getBalance());
		Assert.assertEquals(new Double(1970d), createUser.currentMonthBalance().getBalance());
		Assert.assertEquals(new Double(970d), createUser.getRemainingCredits());
		Assert.assertEquals(new Double(1970), createUser.monthBalanceOf(LocalDate.now().getMonthValue()).getBalance());
		Assert.assertEquals(new Double(1000), createUser.getBalanceLeaderBoard());
		
		
		new ExecutorsBetHelper().scheduleFetchLeaderBoard(0, 3 * 10, TimeUnit.SECONDS);

		Thread.sleep(8000);
		
		createUser = new MongoClientHelperImpl().getUser(createUser.getMongoId(), 0, 0, false, false,
				true, false);
		
		Assert.assertEquals(new Double(1970 - 970), createUser.getBalanceLeaderBoard());

		new MongoClientHelperImpl().deleteUser(createUser.getMongoId());
	}

	@Test
	public void testSettleDealayed() throws InterruptedException {
		new ExecutorsBetHelper().scheduleSettleDelayedPredictions();
		Thread.sleep(1000000);
	}

	@Test
	public void testFetchTeams() throws InterruptedException {
		new MongoTransactionalBlock() {

			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();

				List<Team> teamsFromDb = mongoClientHelperImpl.getTeamsFromDb(session);
//				teamsFromDb.forEach(l->FootballApiCache.ALL_TEAMS.put(l.getId(), l));

				MongoUtils.DB_DATA_FETCHED = true;

			}
		}.execute();

	}

	private void settleBetsInThreads(int threadCount, String mongoId) {
		// bets

		ExecutorService subscribersBetSettling = Executors.newFixedThreadPool(threadCount);// (UserBetHandler.NUM_WORKERS);

		Runnable settleBetsRunnableOrchestrator = () -> {

			try {

				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				Bson pendingOrPendingLostBetsFilter = mongoClientHelperImpl.pendingOrPendingLostBetsFilter();
				Bson finalFilter = null;
				Bson pendingOfUserFilter = null;
				if (mongoId != null)
					pendingOfUserFilter = Filters.eq(MongoFields.MONGO_USER_ID, mongoId);

				if (pendingOfUserFilter != null)
					finalFilter = Filters.and(pendingOrPendingLostBetsFilter, pendingOfUserFilter);
				else
					finalFilter = pendingOrPendingLostBetsFilter;

				long allUnsettledBetsSize = mongoClientHelperImpl.fetchFilterSize(MongoCollectionConstants.USER_BETS,
						finalFilter);
				System.out.println("ALL UNSETTLED BETS:::" + allUnsettledBetsSize);
				if (allUnsettledBetsSize == 0) {
					System.out.println("NOTHING TO SETTLE");
					return;
				}

				long batchSize = (allUnsettledBetsSize / threadCount) > 0 ? allUnsettledBetsSize / threadCount
						: allUnsettledBetsSize % threadCount;

				FindIterable<Document> iterable = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS)
						.find(pendingOrPendingLostBetsFilter).batchSize((int) batchSize);

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

			} catch (Exception e) {
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

	private void settleEvent(int eventId, MatchEventStatus settle, PredictionType predictionType)
			throws InterruptedException {
		MatchEvent eventWon = FootballApiCache.ALL_EVENTS.get(eventId);
		eventWon.setStatus(settle.getStatusStr());
		eventWon.setWinner_code(predictionType.getCode());

		if (MatchEventStatus.POSTPONED == settle) {
			FootballApiCache.WITHDRAWN_EVENTS.put(eventWon);
		} else {
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

		long threeDaysBefore = 3 * 1000 * 24 * 60 * 60;

		return mHelper.getUser(user.getMongoId(), 10, threeDaysBefore, true, true, true, true);
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
