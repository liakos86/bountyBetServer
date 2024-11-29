package gr.server.data.user.model.objects;

import java.io.Serializable;


public class UserAward
implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Unique object id as defined by mongoDb during insert.
	 */
	String mongoId;
	
	int awardMonth;
	
	int awardYear;
	
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

	public int getAwardMonth() {
		return awardMonth;
	}

	public void setAwardMonth(int awardMonth) {
		this.awardMonth = awardMonth;
	}

	public int getAwardYear() {
		return awardYear;
	}

	public void setAwardYear(int awardYear) {
		this.awardYear = awardYear;
	}
	
	
	
}
