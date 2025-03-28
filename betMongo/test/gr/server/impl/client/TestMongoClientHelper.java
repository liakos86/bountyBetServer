package gr.server.impl.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.junit.BeforeClass;
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
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.bet.enums.PredictionType;
import gr.server.data.constants.MongoFields;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.global.helper.ApiDataFetchHelper;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserMonthlyBalance;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.data.user.model.objects.UserPurchase;
import gr.server.mongo.bean.PlaceBetResponseBean;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.ExecutorsBetHelper;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class TestMongoClientHelper {

	final double odd_value = 3.5d;

	Team homeTeam;

	Team awayTeam;
	
	User user;

	List<User> users = new ArrayList<>();
	
	MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
	
	MatchEvent notStartedEvent;

	@BeforeClass
	public static void staticSetup() {
		String s = "mongodb://bountyBetUser:a7fdy4hTXZWeL1kP@192.168.1.4:27017/?authSource=bountyBetDB";
	    
		System.setProperty("MONGO_CONNECTION_STRING", s);
	}
	
	@Before
	public void setup() {
		
		if(homeTeam == null && awayTeam == null) {
			homeTeam = new Team();
			homeTeam.setId(1);
			awayTeam = new Team();
			awayTeam.setId(2);
		}
		
		if(user == null) {
			user = createUser();
			validateUser(user);
		}
		
		if (notStartedEvent == null) {
			Integer matchEventId = 395975;
			notStartedEvent = createEvent(matchEventId, MatchEventStatus.NOTSTARTED);
		}
		
	}

	@After
	public void clear() {
		if (user != null) {
			mHelper.deleteUser(user.getMongoId());
		}
		
		if (homeTeam != null && awayTeam != null) {
			mHelper.deleteTeam(homeTeam);
			mHelper.deleteTeam(awayTeam);
		}
	}

	/**
	 * Creates a new user.
	 * Places a bet for the new user.
	 * Reads the user again to verify bet fields.
	 * Deletes the user.
	 * Verifies deletion.
	 */
	@Test
	public void testPlaceBet_success(){
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		UserPrediction pred = new UserPrediction();
		pred.setEventId(notStartedEvent.getId());
		pred.setPredictionCategory(PredictionCategory.FINAL_RESULT);
		pred.setPredictionType(PredictionType.AWAY_WIN);
		pred.setOddValue(odd_value);
		preds.add(pred);
		
		
		userBet.setPredictions(preds);
		PlaceBetResponseBean betPlacementStatusBean = mHelper.placeBet(userBet);
		Assert.assertEquals(BetPlacementStatus.PLACED, betPlacementStatusBean.getBetPlacementStatus());
		
		Assert.assertTrue(userBet.getMongoId() != null);
		
		user = mHelper.getUserFull(user.getMongoId());
		Assert.assertEquals(1, user.getUserBets().size());
		UserBet retrievedBet = user.getUserBets().get(0);
		Assert.assertEquals(userBet.getMongoId(), retrievedBet.getMongoId());
		Assert.assertEquals(30d, userBet.getBetAmount(), 0d);
		Assert.assertEquals(BetStatus.PENDING.getCode(), retrievedBet.getBetStatus());
	
		mHelper.deleteUser(user.getMongoId());
		user = mHelper.getUserFull(user.getMongoId());
		Assert.assertNull(user);
		
	}
	
	
	/**
	 * Creates a new user.
	 * Places a bet for the new user.
	 * Reads the user again to verify bet fields.
	 * Deletes the user.
	 * Verifies deletion.
	 * @throws Exception 
	 */
	@SuppressWarnings("removal")
	@Test
	public void testSettleBetLost() throws Exception{
		Set<MatchEvent> events = new HashSet<>();
		MatchEvent eventWon = createEvent(1, MatchEventStatus.NOTSTARTED);
		events.add(eventWon);
		MatchEvent eventLost = createEvent(2, MatchEventStatus.NOTSTARTED);
		events.add(eventLost);
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = createUserPrediction(1, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, odd_value);
		preds.add(predCorrect);
		
		UserPrediction predWrong = createUserPrediction(2, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, 2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.DRAW.getCode());

		mHelper.settlePredictions(events);

		
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
		
		Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));

		FindIterable<Document> find = betsCollection.find(betFilter);
		
		mHelper.settleOpenBets(Set.of(find.first()));
		user = mHelper.getUserFull(user.getMongoId());
		
		
		Optional<UserBet> findFirst = user.getUserBets().stream().filter(b -> b.getMongoId().equals(userBet.getMongoId())).findFirst();
		
		Assert.assertEquals(BetStatus.SETTLED_UNFAVOURABLY.getCode(), findFirst.get().getBetStatus());
		
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
	 * @throws Exception 
	 */
	@Test
	public void testSettleBetLostInMultipleSteps() throws Exception{

		Set<MatchEvent> events = new HashSet<>();
		
		MatchEvent eventWon = createEvent(1, MatchEventStatus.NOTSTARTED); 
		events.add(eventWon);
		
		MatchEvent eventLost = createEvent(2, MatchEventStatus.NOTSTARTED);
		events.add(eventLost);
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = createUserPrediction(1, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, odd_value);
		preds.add(predCorrect);
		
		UserPrediction predWrong = createUserPrediction(2, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, 2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.DRAW.getCode());


		mHelper.settlePredictions(Set.of(eventLost));
	
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
    	FindIterable<Document> find = betsCollection.find(betFilter);
		mHelper.settleOpenBets(Set.of(find.first()));

		User userAfterFirstStep = mHelper.getUserFull(user.getMongoId());

		
		Assert.assertEquals(new Integer(1), userAfterFirstStep.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(0), userAfterFirstStep.getOverallWonEventsCount());
		Assert.assertEquals(BetStatus.PENDING_LOST.getCode(), userAfterFirstStep.getUserBets().get(0).getBetStatus());
		
		
		//second step
		
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		
		mHelper.settlePredictions(events);
		
    	find = betsCollection.find(betFilter);
		mHelper.settleOpenBets(Set.of(find.first()));
		
		
		user = mHelper.getUserFull(user.getMongoId());
		
		mHelper.deleteUser(user.getMongoId());
		
		Assert.assertEquals(BetStatus.SETTLED_UNFAVOURABLY.getCode(), user.getUserBets().get(0).getBetStatus());
		
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
		
	}

	/**
	 * Creates a new user.
	 * Places a bet with two predictions for the new user.
	 * Settles the same prediction twice.
	 * The prediction is won.
	 * The bet should be pending.
	 * Deletes the user.
	 * @throws Exception 
	 */
	@Test
	public void testSettleBetLostInMultipleStepsStillPending() throws Exception{
		
		Set<MatchEvent> events = new HashSet<>();
		MatchEvent eventWon = createEvent(1, MatchEventStatus.NOTSTARTED);
		events.add(eventWon);
		
		MatchEvent eventLost = createEvent(2, MatchEventStatus.NOTSTARTED);
		events.add(eventLost);
		
		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(30);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		
		UserPrediction predCorrect = createUserPrediction(1, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, odd_value);
		preds.add(predCorrect);
		
		UserPrediction predWrong = createUserPrediction(2, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, 2.5d);
		preds.add(predWrong);
		
		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);
		
		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());

		mHelper.settlePredictions(Set.of(eventWon));
		MongoCollection<Document> betsCollection = MongoUtils.getMongoCollection(MongoCollectionConstants.USER_BETS);
    	
    	Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
    	FindIterable<Document> find = betsCollection.find(betFilter);
		mHelper.settleOpenBets(Set.of(find.first()));
		
		user = mHelper.getUserFull(user.getMongoId());
		
		Assert.assertEquals(new Integer(0), user.getOverallLostEventsCount());
		Assert.assertEquals(new Integer(1), user.getOverallWonEventsCount());
		Assert.assertEquals(BetStatus.PENDING.getCode(), user.getUserBets().get(0).getBetStatus());
		
		
		//second step
		
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setWinner_code(PredictionType.HOME_WIN.getCode());
		
    	betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
    	find = betsCollection.find(betFilter);
    	mHelper.settleOpenBets(Set.of(find.first()));
		
		user = mHelper.getUserFull(user.getMongoId());
		
		Assert.assertEquals(BetStatus.PENDING.getCode(), user.getUserBets().get(0).getBetStatus());
		
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
	}
	
	/**
	 * Creates a new user. Places a bet for the new user. Reads the user again to
	 * verify bet fields. Deletes the user. Verifies deletion.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSettleBetWon() throws Exception {

		Set<MatchEvent> events =  new HashSet<>();
		MatchEvent eventWon = createEvent(1, MatchEventStatus.NOTSTARTED); 
		events.add(eventWon);

		MatchEvent eventLost = createEvent(2, MatchEventStatus.NOTSTARTED); 
		events.add(eventLost);

		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();

		UserBet userBet = new UserBet();
		userBet.setMongoUserId(user.getMongoId());
		userBet.setBetAmount(800);

		List<UserPrediction> preds = new ArrayList<UserPrediction>();

		UserPrediction predCorrect = createUserPrediction(1, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, odd_value);
		preds.add(predCorrect);

		UserPrediction predWrong = createUserPrediction(2, PredictionCategory.FINAL_RESULT, PredictionType.AWAY_WIN, 2.5d);
		preds.add(predWrong);

		userBet.setPredictions(preds);
		mHelper.placeBet(userBet);

		user = mHelper.getUserFull(user.getMongoId());

		Double newBalance = 200d;

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));

		eventWon.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventLost.setStatus(MatchEventStatus.FINISHED.getStatusStr());
		eventWon.setWinner_code(PredictionType.AWAY_WIN.getCode());
		eventLost.setWinner_code(PredictionType.AWAY_WIN.getCode());

		mHelper.settlePredictions(Set.of(eventLost, eventWon));

		MongoCollection<Document> betsCollection = MongoUtils
				.getMongoCollection(MongoCollectionConstants.USER_BETS);

		Bson betFilter = Filters.eq(MongoFields.MONGO_ID, new ObjectId(userBet.getMongoId()));
		FindIterable<Document> find = betsCollection.find(betFilter);

		mHelper.settleOpenBets(Set.of(find.first()));
		
		user = mHelper.getUserFull(user.getMongoId());
		
		Assert.assertEquals(BetStatus.SETTLED_FAVOURABLY.getCode(), user.getUserBets().get(0).getBetStatus());
		
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

		createEvent(10, MatchEventStatus.NOTSTARTED);

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

		user = mHelper.getUserFull(user.getMongoId());

		Double newBalance = 1000d - (24 * 30);

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));

		settlePredictionsInThreads(1);
		settleBetsInThreads(4, user.getMongoId());

		Thread.sleep(20000);

		user = mHelper.getUserFull(user.getMongoId());

		newBalance = newBalance + (16 * 30 * odd_value);// odd

		Assert.assertEquals(newBalance, Double.valueOf(user.getBalance()));
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

		Thread.sleep(5000);

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
		createEvent(1, MatchEventStatus.NOTSTARTED);
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
		
		
		createEvent(1, MatchEventStatus.NOTSTARTED);
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

	
	
	@Test
	public void testGetStatsAndIncidents(){
		ApiDataFetchHelper ap = new ApiDataFetchHelper();
		ap.fetchEventStatistics(Set.of(1));
	}
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

//	void createEvent(int eventId, MatchEventStatus status) throws InterruptedException {
//		MatchEvent eventWon = new MatchEvent();
//		eventWon.setId(eventId);
//		eventWon.setHome_team(homeTeam);
//		eventWon.setAway_team(awayTeam);
//		eventWon.setStart_at(new SimpleDateFormat(ServerConstants.DATE_WITH_TIME_FORMAT).format(new Date()));
//		eventWon.setStatus(status.getStatusStr());
//
//		FootballApiCache.ALL_EVENTS.put(eventWon.getId(), eventWon);
//
//	}

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

		UserPrediction predCorrect = createUserPrediction(eventId, PredictionCategory.FINAL_RESULT, predictionType, odd_value);
		preds.add(predCorrect);

		userBet.setPredictions(preds);

		mHelper.placeBet(userBet);

	}

	User createUser() {
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		User user = new User();
		user.setUsername("TestUser" + System.currentTimeMillis());
		user.setPassword("pass");
		user.setValidated(true);
		user.setEmail(System.currentTimeMillis() + "test@test.gr");
		return mHelper.createUser(user);
	}
	
	void validateUser(User user) {
		mHelper.validateUser(user.getEmail());
	}

	MatchEvent createEvent(Integer eventId, MatchEventStatus status) {
		if (FootballApiCache.ALL_EVENTS.containsKey(eventId)) {
			System.out.println("Event in cache already " + eventId);
			return FootballApiCache.ALL_EVENTS.get(eventId);
		}
		
		SimpleDateFormat matchDateFormat = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT);
		
		MatchEvent event = new MatchEvent();
		event.setStart_at(matchDateFormat.format(new Date()));
		event.setId(eventId);
		event.setStatus(status.getStatusStr());
		event.setHome_team(homeTeam);
		event.setAway_team(awayTeam);
		
		FootballApiCache.ALL_EVENTS.put(event.getId(), event);
		return event;
	}

	Team createTeam(int teamId, String teamName) {
		Team team = new Team();
		team.setId(teamId);
		team.setName(teamName);
		return team;
	}
	
	private UserPrediction createUserPrediction(int id, PredictionCategory finalResult, PredictionType pred,
			double odd) {
		UserPrediction predCorrect = new UserPrediction();
		predCorrect.setEventId(id);
		predCorrect.setPredictionCategory(finalResult);
		predCorrect.setPredictionType(pred);
		predCorrect.setOddValue(odd);
		return predCorrect;
	}

}
