package gr.server.def.client;

import java.text.ParseException;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.ClientSession;

import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.events.Event;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Odd;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.impl.client.ApiFootballClient;

public interface MongoClientHelper {

	/**
	 * A {@link User} places a new {@link UserBet} that involves a list of {@link UserPrediction}s. 
	 * 
	 * @param userBet
	 * @return
	 */
	public UserBet placeBet(UserBet userBet);
	
	/**
	 * For every open {@link UserBet}, the system will iterate through its {@link UserPrediction}s
	 * and will settle it favourably or not, depending on the FT status of all its predictions.
	 * 
	 */
	void settleBets();
	
	/**
	 * Creates a new {@link User} in the 'user' collection of the database.
	 * In case a {@link Document} with the same username already exists,
	 * a {@link UserExistsException} is thrown.
	 * 
	 * @param user
	 * @return
	 * @throws UserExistsException
	 */
	public User createUser(User user) throws UserExistsException;
	
	/**
	 * Called to fetch the open bets for a {@link User}.
	 * 
	 * @param id the user id
	 * @return
	 */
	User getUser(String id);
	
	/**
	 * {@link ApiFootballClient} will call this method to store the newly fetched leagues with their competitions, events, etc.
	 * 
	 * @param leagues
	 */
	void storeLeagues(ClientSession session, List<League> leagues) throws ParseException;
	
	/**
	 * {@link MyBetOddsServiceImpl} will call this method upon user's REST call.
	 * 
	 * Returns a list of {@link CountryWithCompetitions}.
	 * Every competition contains a list of {@link Event}s,
	 * which in turns contains an {@link Odd} for the event.
	 * 
	 * @return
	 */
	List<League> getMongoLeagues();

}
