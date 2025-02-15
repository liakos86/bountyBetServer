package gr.server.mongo.bean;

import java.io.Serializable;

import gr.server.data.bet.enums.BetPlacementStatus;

public class PlaceBetResponseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String betId;
	
	BetPlacementStatus betPlacementStatus;

	public String getBetId() {
		return betId;
	}

	public void setBetId(String betId) {
		this.betId = betId;
	}

	public BetPlacementStatus getBetPlacementStatus() {
		return betPlacementStatus;
	}

	public void setBetPlacementStatus(BetPlacementStatus betPlacementStatus) {
		this.betPlacementStatus = betPlacementStatus;
	}
	
}
