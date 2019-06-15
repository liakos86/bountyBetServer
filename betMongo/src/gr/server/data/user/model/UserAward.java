package gr.server.data.user.model;

import java.io.Serializable;


public class UserAward
implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Unique object id as defined by mongoDb during insert.
	 */
	String mongoId;
	
	String awardMonth;
	
	Double winningBalance;
	
	public Double getWinningBalance() {
		return winningBalance;
	}

	public void setWinningBalance(Double winningBalance) {
		this.winningBalance = winningBalance;
	}

	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public String getAwardMonth() {
		return awardMonth;
	}

	public void setAwardMonth(String awardMonth) {
		this.awardMonth = awardMonth;
	}
	
}
