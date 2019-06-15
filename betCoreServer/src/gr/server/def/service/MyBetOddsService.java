package gr.server.def.service;

import gr.server.application.exception.UserExistsException;
import gr.server.data.user.model.UserBet;

import java.io.InputStream;
import java.util.List;

/**
 * Rest service for sending data to the mobile devices.
 *  
 */
public interface MyBetOddsService {

	
	String placeBet(InputStream incoming);
	
	String createUser(InputStream incomingStream) throws UserExistsException;

	List<UserBet> getMyOpenBets(String id);

	String getLeagues();

	String getUser(String id);

	String getLeaderBoard();

}
