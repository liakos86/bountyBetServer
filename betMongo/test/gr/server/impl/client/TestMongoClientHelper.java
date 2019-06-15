package gr.server.impl.client;

import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.events.Event;
import gr.server.data.api.model.league.League;
import gr.server.data.constants.CollectionNames;
import gr.server.data.constants.ApiFootBallConstants;
import gr.server.data.user.model.User;
import gr.server.data.user.model.UserBet;
import gr.server.data.user.model.UserPrediction;
import gr.server.mongo.util.DateUtils;
import gr.server.mongo.util.Executor;
import gr.server.mongo.util.MongoCollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

public class TestMongoClientHelper {
	
	/**
	 * Calls mongo db in order to store a new document in bets collection
	 */
	@Test
	public void testPlacePrediction(){
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		UserBet userBet = new UserBet();
		userBet.setMongoUserId("user1");
		userBet.setBetAmount(30);
		userBet.setBetStatus(1);
		
		List<UserPrediction> preds = new ArrayList<UserPrediction>();
		UserPrediction pred = new UserPrediction();
		pred.setEventId("395975");
		pred.setPrediction(1);
		preds.add(pred);
		userBet.setPredictions(preds);
		userBet = mHelper.placeBet(userBet);
		System.out.println(userBet.getMongoUserId());//(placePrediction.getObjectId("_id"));
	}
	
	/**
	 * Calls mongo db in order to store a new {@link User} in user collection
	 * @throws UserExistsException 
	 */
	@Test//(expected=UserExistsException.class)
	public void testCreateUser() throws UserExistsException{
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		User user = new User();
		user.setUsername("kostas");
		mHelper.createUser(user);
	}
	
	/**
	 * Calls mongo db in order to retrieve {@link User}'s open bets
	 */
	@Test
	public void testGetOpenBets() throws UserExistsException{
		MongoClientHelperImpl mHelper = new MongoClientHelperImpl();
		User jim = mHelper.getUser("5c718c6c67a90b10dc34c244");
		System.out.println(jim.getUserBets().get(0).getPredictions().get(0).getPrediction());
	}
	
	@Test
	public void testGetCountriesWithCompetitions(){
		List<League> retrieveCompetitionsWithEventsAndOdds = new MongoClientHelperImpl().getLeagues();
		
		System.out.println(retrieveCompetitionsWithEventsAndOdds.size());
	}
	
	@Test
	public void testGetPreviousMonthAsString(){
		System.out.println(DateUtils.getPastMonthAsString(1));
		System.out.println(DateUtils.isFirstDayOfMonth());
	}
	
	@Test
	public void testISO() throws ParseException{
		
		
		Document search = new Document("match_id", "408642");
		Executor<Event> betsExecutor = new Executor<Event>(new TypeToken<Event>() { });
		List<Event> bets = MongoCollectionUtils.get(CollectionNames.EVENTS, search, betsExecutor);
		Event event = bets.get(0);
		String inpt = event.getMatchDate()+ " "+event.getMatchTime();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);

	    ZonedDateTime madridTime = LocalDateTime.parse(inpt, dtf)
	            .atOffset(ZoneOffset.UTC)
	            .atZoneSameInstant(ZoneId.systemDefault());
		
	    System.out.println(madridTime.toString());
	    
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ApiFootBallConstants.EVENT_DATE_TIME_FORMAT);
		System.out.println(simpleDateFormat.format(new Date(bets.get(0).getMatchFullDate().get$numberLong())));
	}
	
}
