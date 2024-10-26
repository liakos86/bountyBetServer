package gr.server.data.bet.enums;

import java.io.Serializable;

public enum BetPlacementStatus implements Serializable {
	
	PLACED(1),
	
	FAILED_MATCH_IN_PROGRESS(2),
	
	FAIL_GENERIC(3), 
	
	FAILED_INSUFFICIENT_FUNDS(4),
	
	FAILED_USER_NOT_VALIDATED(5);
	
	private static final long serialVersionUID = 1L;
	private int code;

	private BetPlacementStatus(int code) {
		this.setCode(code);
	}
	
	public static BetPlacementStatus fromCode(int code) throws Exception {
		for(BetPlacementStatus status : BetPlacementStatus.values()){
			if (code == status.getCode()){
				return status;
			}
		}
		throw new Exception("Bet placement code: "+code+" not found");
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}

