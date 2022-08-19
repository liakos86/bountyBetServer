package gr.server.data.user.model.objects;


import gr.server.data.constants.ApiFootBallConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	/**
	 * Unique object id as defined by mongoDb during insert.
	 */
	String mongoId;

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
	
	public User(){}

	public User(String mongoId) {
		this.mongoId = mongoId;
		this.balance = ApiFootBallConstants.STARTING_BALANCE;
		this.lostEventsCount = 0;
		this.lostSlipsCount = 0;
		this.wonEventsCount = 0;
		this.wonSlipsCount = 0;
		this.overallLostEventsCount = 0;
		this.overallLostSlipsCount = 0;
		this.overallWonEventsCount = 0;
		this.overallWonSlipsCount = 0;
		this.userBets = new ArrayList<UserBet>();
		this.userAwards = new ArrayList<UserAward>();
		this.bounties = new ArrayList<UserBounty>();
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
	
}
