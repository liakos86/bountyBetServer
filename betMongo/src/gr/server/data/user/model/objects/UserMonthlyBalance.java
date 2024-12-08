package gr.server.data.user.model.objects;

import java.io.Serializable;

import gr.server.common.ServerConstants;

public class UserMonthlyBalance implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int month;
	private int monthlyLostEventsCount;
	private int monthlyLostSlipsCount;
	private int monthlyWonEventsCount;
	private int monthlyWonSlipsCount;

	private Double balance;

	public UserMonthlyBalance(int month) {
		this.balance = ServerConstants.STARTING_BALANCE;
		this.monthlyLostEventsCount = 0;
		this.monthlyLostSlipsCount = 0;
		this.monthlyWonEventsCount = 0;
		this.monthlyWonSlipsCount = 0;
		
		if (1 <= month && 12 >= month) {
			this.month = month;
			return;
		}
		
		throw new RuntimeException("MONTH OUT OF BOUNDS");
	}

	public int getMonthlyLostEventsCount() {
		return monthlyLostEventsCount;
	}

	public void setMonthlyLostEventsCount(int monthlyLostEventsCount) {
		this.monthlyLostEventsCount = monthlyLostEventsCount;
	}

	public int getMonthlyLostSlipsCount() {
		return monthlyLostSlipsCount;
	}

	public void setMonthlyLostSlipsCount(int monthlyLostSlipsCount) {
		this.monthlyLostSlipsCount = monthlyLostSlipsCount;
	}

	public int getMonthlyWonEventsCount() {
		return monthlyWonEventsCount;
	}

	public void setMonthlyWonEventsCount(int monthlyWonEventsCount) {
		this.monthlyWonEventsCount = monthlyWonEventsCount;
	}

	public int getMonthlyWonSlipsCount() {
		return monthlyWonSlipsCount;
	}

	public void setMonthlyWonSlipsCount(int monthlyWonSlipsCount) {
		this.monthlyWonSlipsCount = monthlyWonSlipsCount;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}
	

}
