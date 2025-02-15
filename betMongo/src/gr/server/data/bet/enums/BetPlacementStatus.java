package gr.server.data.bet.enums;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum BetPlacementStatus implements Serializable {
	
	@SerializedName("PLACED")
	PLACED(1),
	
	@SerializedName("FAILED_MATCH_IN_PROGRESS")
	FAILED_MATCH_IN_PROGRESS(2),
	
	@SerializedName("FAIL_GENERIC")
	FAIL_GENERIC(3), 
	
	@SerializedName("FAILED_INSUFFICIENT_FUNDS")
	FAILED_INSUFFICIENT_FUNDS(4),
	
	@SerializedName("FAILED_USER_NOT_VALIDATED")
	FAILED_USER_NOT_VALIDATED(5),
	
	@SerializedName("FAILED_MATCH_IN_NEXT_MONTH")
	FAILED_MATCH_IN_NEXT_MONTH(6);
	
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

