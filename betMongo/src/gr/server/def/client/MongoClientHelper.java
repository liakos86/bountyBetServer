package gr.server.def.client;

import java.util.Set;

import org.bson.Document;

import gr.server.application.exception.UserExistsException;
import gr.server.data.api.model.dto.LoginResponseDto;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.bet.enums.BetPlacementStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;

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
	 * Called to fetch a user with desired metadata for a {@link User}.
	 * 
	 * @param id the user id
	 * @return
	 */
	User getUser(String id, int maxBetsToFetch, long millisToSearchBets, boolean includeAwards, boolean includeBalances,
			boolean includeBounties);

	/**
	 * Retrieves a user via login.
	 * Later {@link #getUser(String)} will be used.
	 * 
	 * @param user
	 * @return
	 */
	LoginResponseDto loginUser(String username, String email, String password);

	boolean settlePredictions(Set<MatchEvent> toSettle) throws Exception;
	
	boolean settleWithdrawnPredictions(Set<MatchEvent> toSettle) throws Exception;

	boolean settleOpenBets(Set<Document> pendingBets) throws Exception;

	void deleteUser(String mongoId);
	

	BetPlacementStatus placeBet(UserBet userBet);

//	Set<MatchEvent> getLiveByIds(String ids);

	boolean closeMonthlyBalancesAndComputeMonthWinner();
}
