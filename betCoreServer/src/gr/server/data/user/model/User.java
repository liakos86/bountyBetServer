package gr.server.data.user.model;


import gr.server.data.constants.ApiFootBallConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User of the application.
 * 
 */
public class User
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
	 * Each user has a balance, i.e. a virtual amount.
	 */
	Double balance;
	
	/**
	 * Number of won slips.
	 */
	Integer wonSlipsCount;
	
	/**
	 * Number of lost slips.
	 */
	Integer lostSlipsCount;
	
	/**
	 * Number of won events.
	 */
	Integer wonEventsCount;
	
	/**
	 * Number of lost events.
	 */
	Integer lostEventsCount;
	
	List<UserBet> userBets;
	
	Long position;
	
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

	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
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

	public Integer getWonSlipsCount() {
		return wonSlipsCount;
	}

	public void setWonSlipsCount(Integer wonSlipsCount) {
		this.wonSlipsCount = wonSlipsCount;
	}

	public Integer getLostSlipsCount() {
		return lostSlipsCount;
	}

	public void setLostSlipsCount(Integer lostSlipsCount) {
		this.lostSlipsCount = lostSlipsCount;
	}

	public Integer getWonEventsCount() {
		return wonEventsCount;
	}

	public void setWonEventsCount(Integer wonEventsCount) {
		this.wonEventsCount = wonEventsCount;
	}

	public Integer getLostEventsCount() {
		return lostEventsCount;
	}

	public void setLostEventsCount(Integer lostEventsCount) {
		this.lostEventsCount = lostEventsCount;
	}

	public List<UserBounty> getBounties() {
		return bounties;
	}

	public void setBounties(List<UserBounty> bounties) {
		this.bounties = bounties;
	}
	
}
