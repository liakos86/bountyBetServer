package gr.server.data.bet.enums;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum BetStatus implements Serializable {
	
	@SerializedName("1")
	PENDING(1, "Pending"),
	
	@SerializedName("2")
	SETTLED_FAVOURABLY(2, "Won"),
	
	@SerializedName("3")
	SETTLED_UNFAVOURABLY(3, "Lost"),
	
	@SerializedName("4")
	PENDING_LOST(4, "Lost");
	
	private static final long serialVersionUID = 1L;
	private String statusString;
	private int code;

	private BetStatus(int code, String statusString) {
		this.setCode(code);
		this.setStatusString(statusString);
	}
	
	public PredictionStatus fromCode(int code) throws Exception {
		for(PredictionStatus status : PredictionStatus.values()){
			if (code == status.getCode()){
				return status;
			}
		}
		throw new Exception("Prediction code: "+code+" not found");
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}

