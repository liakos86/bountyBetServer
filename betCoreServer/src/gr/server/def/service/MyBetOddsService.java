package gr.server.def.service;

import java.util.List;

import javax.ws.rs.core.Response;

import gr.server.application.exception.UserExistsException;
import gr.server.data.user.model.objects.UserBet;

/**
 * Rest service for sending data to the mobile devices.
 *  
 */
public interface MyBetOddsService {

	
//	String placeBet(InputStream incoming);
	
//	String createUser(InputStream incomingStream) throws UserExistsException;

	List<UserBet> getMyOpenBets(String id);

	String getLeagues();

	Response getUser(String id);

	String getLeaderBoard();

	Response placeBet(String userBet);

	Response createUser(String user) throws UserExistsException;

	String getLive();

	String getLiveUpdates();


}
