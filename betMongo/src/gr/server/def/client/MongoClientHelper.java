package gr.server.def.client;

import java.util.Set;

import org.bson.Document;

import com.mongodb.client.ClientSession;

import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.user.model.objects.User;

public interface MongoClientHelper {

	/**
	 * Creates a new {@link User} in the 'user' collection of the database.
	 * In case a {@link Document} with the same username already exists,
	 * a {@link UserExistsException} is thrown.
	 * 
	 * @param user
	 * @return
	 * @throws UserExistsException
	 */
	User createUser(User user);
	
	/**
	 * Creates a new {@link User} in the 'user' collection of the database.
	 * In case a {@link Document} with the same username already exists,
	 * a {@link UserExistsException} is thrown.
	 * 
	 * @param user
	 * @return
	 * @throws UserExistsException
	 */
	void validateUser(String email);
	
	/**
	 * Called to fetch the open bets for a {@link User}.
	 * 
	 * @param id the user id
	 * @return
	 */
	User getUser(String id);

	/**
	 * Retrieves a user via login.
	 * Later {@link #getUser(String)} will be used.
	 * 
	 * @param user
	 * @return
	 */
	User loginUser(User user);

	void settlePredictions(ClientSession session, Set<MatchEvent> events) throws Exception;

	void settleOpenBets(ClientSession session) throws Exception;

	void deleteUser(String mongoId);
	
}
