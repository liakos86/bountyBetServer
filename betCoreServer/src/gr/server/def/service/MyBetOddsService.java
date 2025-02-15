package gr.server.def.service;

import javax.ws.rs.core.Response;

/**
 * Rest service for sending data to the mobile devices.
 *  
 */
public interface MyBetOddsService {

	/**
	 * 
	 * @return Sections with leagues.
	 */
	Response getLeagues();

	Response getUser(String id);

	Response getLeaderBoard();

	Response placeBet(String userBet);

	Response registerUser(String user) throws Exception;
	
	Response loginUser(String user) throws Exception;

	String validateUser(String email) throws Exception;

	Response getEventStatistics(Integer id);
	
	Response getStandingsOfSeason(Integer leagueId, Integer seasonId);

	Response getLeagueEvents();

	Response getSections();

	Response getLiveEvents();
	
	Response verifyPurchase(String verificationString);

	Response authorize(String uniqueDeviceId) throws Exception;

}
