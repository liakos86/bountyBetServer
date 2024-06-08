package gr.server.data.user.model.objects;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gr.server.data.constants.SportScoreApiConstants;

/**
 * User of the application.
 * 
 */
public class User
extends NonMongoUserFields
implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	boolean validated;
	
	/**
	 * 
	 */
	String errorMessage;

	/**
	 * Unique object id as defined by mongoDb during insert.
	 */
	String mongoId;

	/**
	 * The unique email that the app user will demand.
	 */
	String email;
	
	/**
	 * The unique username that the app user will demand.
	 */
	String username;
	
	/**
	 * Password in order to be portable between devices.
	 */
	String password;

	/**
	 * Each user has a balance, i.e. a virtual amount.
	 */
	Double balance;
	
	List<UserBet> userBets;
	
	List<UserAward> userAwards; 
	
	List<String> userAwardsIds;
	
	List<UserBounty> bounties;
	
	int level;
	
	/**
	 * Number of won slips.
	 */
	Integer monthlyWonSlipsCount;
	
	/**
	 * Number of lost slips.
	 */
	Integer monthlyLostSlipsCount;
	
	/**
	 * Number of won events.
	 */
	Integer monthlyWonEventsCount;
	
	/**
	 * Number of lost events.
	 */
	Integer monthlyLostEventsCount;
	
	/**
	 * Total/overall Number of won slips.
	 */
	Integer overallWonSlipsCount;
	
	/**
	 * Total/overall Number of lost slips.
	 */
	Integer overallLostSlipsCount;
	
	/**
	 * Total/overall Number of won events.
	 */
	Integer overallWonEventsCount;
	
	/**
	 * Total/overall Number of lost events.
	 */
	Integer overallLostEventsCount;
	
	
	public User(){}

	public User(String mongoId) {
		this.mongoId = mongoId;
		this.balance = SportScoreApiConstants.STARTING_BALANCE;
		this.monthlyLostEventsCount = 0;
		this.monthlyLostSlipsCount = 0;
		this.monthlyWonEventsCount = 0;
		this.monthlyWonSlipsCount = 0;
		this.overallLostEventsCount = 0;
		this.overallLostSlipsCount = 0;
		this.overallWonEventsCount = 0;
		this.overallWonSlipsCount = 0;
		this.userBets = new ArrayList<UserBet>();
		this.userAwards = new ArrayList<UserAward>();
		this.bounties = new ArrayList<UserBounty>();
	}
	
	public void init() {
		this.balance = SportScoreApiConstants.STARTING_BALANCE;
		this.monthlyLostEventsCount = 0;
		this.monthlyLostSlipsCount = 0;
		this.monthlyWonEventsCount = 0;
		this.monthlyWonSlipsCount = 0;
		this.overallLostEventsCount = 0;
		this.overallLostSlipsCount = 0;
		this.overallWonEventsCount = 0;
		this.overallWonSlipsCount = 0;
		this.userBets = new ArrayList<UserBet>();
		this.userAwards = new ArrayList<UserAward>();
		this.bounties = new ArrayList<UserBounty>();
	}
	
	public Integer getOverallWonSlipsCount() {
		return overallWonSlipsCount;
	}

	public void setOverallWonSlipsCount(Integer overallWonSlipsCount) {
		this.overallWonSlipsCount = overallWonSlipsCount;
	}

	public Integer getOverallLostSlipsCount() {
		return overallLostSlipsCount;
	}

	public void setOverallLostSlipsCount(Integer overallLostSlipsCount) {
		this.overallLostSlipsCount = overallLostSlipsCount;
	}

	public Integer getOverallWonEventsCount() {
		return overallWonEventsCount;
	}

	public void setOverallWonEventsCount(Integer overallWonEventsCount) {
		this.overallWonEventsCount = overallWonEventsCount;
	}

	public Integer getOverallLostEventsCount() {
		return overallLostEventsCount;
	}

	public void setOverallLostEventsCount(Integer overallLostEventsCount) {
		this.overallLostEventsCount = overallLostEventsCount;
	}

	public Integer getMonthlyWonSlipsCount() {
		return monthlyWonSlipsCount;
	}

	public void setMonthlyWonSlipsCount(Integer wonSlipsCount) {
		this.monthlyWonSlipsCount = wonSlipsCount;
	}

	public Integer getMonthlyLostSlipsCount() {
		return monthlyLostSlipsCount;
	}

	public void setMonthlyLostSlipsCount(Integer lostSlipsCount) {
		this.monthlyLostSlipsCount = lostSlipsCount;
	}

	public Integer getMonthlyWonEventsCount() {
		return monthlyWonEventsCount;
	}

	public void setMonthlyWonEventsCount(Integer wonEventsCount) {
		this.monthlyWonEventsCount = wonEventsCount;
	}

	public Integer getMonthlyLostEventsCount() {
		return monthlyLostEventsCount;
	}

	public void setMonthlyLostEventsCount(Integer lostEventsCount) {
		this.monthlyLostEventsCount = lostEventsCount;
	}
	
	public List<String> getUserAwardsIds() {
		return userAwardsIds;
	}

	public void setUserAwardsIds(List<String> userAwardsIds) {
		this.userAwardsIds = userAwardsIds;
	}

	public List<UserAward> getUserAwards() {
		return userAwards;
	}

	public void setUserAwards(List<UserAward> userAwards) {
		this.userAwards = userAwards;
	}
	
	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public List<UserBet> getUserBets() {
		return userBets;
	}

	public void setUserBets(List<UserBet> userBets) {
		this.userBets = userBets;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public List<UserBounty> getBounties() {
		return bounties;
	}

	public void setBounties(List<UserBounty> bounties) {
		this.bounties = bounties;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
}
