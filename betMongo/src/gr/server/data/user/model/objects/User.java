package gr.server.data.user.model.objects;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	 * We store history dating back 12 months. Every user will have 12 records.
	 */
//	@JsonIgnore
	List<UserMonthlyBalance> balances;
	
	
	List<UserBet> userBets;
	
	List<UserAward> userAwards; 
	
	List<String> userAwardsIds;
	
	List<UserBounty> bounties;
	
	int level;
	
	double balance;
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
		
		this.overallLostEventsCount = 0;
		this.overallLostSlipsCount = 0;
		this.overallWonEventsCount = 0;
		this.overallWonSlipsCount = 0;
		this.userBets = new ArrayList<UserBet>();
		this.userAwards = new ArrayList<UserAward>();
		this.bounties = new ArrayList<UserBounty>();
		this.balances = new ArrayList<>();
	}
	
	public void init() {
		
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
	
	

	public Integer getMonthlyWonSlipsCount() {
		return monthlyWonSlipsCount;
	}

	public void setMonthlyWonSlipsCount(Integer monthlyWonSlipsCount) {
		this.monthlyWonSlipsCount = monthlyWonSlipsCount;
	}

	public Integer getMonthlyLostSlipsCount() {
		return monthlyLostSlipsCount;
	}

	public void setMonthlyLostSlipsCount(Integer monthlyLostSlipsCount) {
		this.monthlyLostSlipsCount = monthlyLostSlipsCount;
	}

	public Integer getMonthlyWonEventsCount() {
		return monthlyWonEventsCount;
	}

	public void setMonthlyWonEventsCount(Integer monthlyWonEventsCount) {
		this.monthlyWonEventsCount = monthlyWonEventsCount;
	}

	public Integer getMonthlyLostEventsCount() {
		return monthlyLostEventsCount;
	}

	public void setMonthlyLostEventsCount(Integer monthlyLostEventsCount) {
		this.monthlyLostEventsCount = monthlyLostEventsCount;
	}

	public List<UserMonthlyBalance> getBalances() {
		return balances;
	}

	public void setBalances(List<UserMonthlyBalance> balances) {
		this.balances = balances;
	}
	
	

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getBalance() {
		return currentMonthBalance();
	}

	public double currentMonthBalance() {
		return monthBalanceOf(LocalDate.now().getMonthValue());
	}

	public double monthBalanceOf(Integer belongingMonth) {
		if (1 > belongingMonth || 12 < belongingMonth) {
			throw new RuntimeException("MONTH RANGE ERROR");
		}
		
		List<UserMonthlyBalance> monthBalances = balances.stream().filter(b -> b.getMonth() == belongingMonth).collect(Collectors.toList());
		if (monthBalances.size() != 1) {
			throw new RuntimeException("MONTH RANGE COUNT ERROR");
		}
		
		return monthBalances.get(0).getBalance();
	}
	
	public UserMonthlyBalance currentBalanceObject() {
		return  balances.stream().filter(b -> b.getMonth() == LocalDate.now().getMonthValue()).collect(Collectors.toList()).get(0);
	}
	
	@Override
	public String toString() {
		return "USERNAME " + this.username + " EMAIL " + this.email + " VALID " + this.validated;
	}

		
}
