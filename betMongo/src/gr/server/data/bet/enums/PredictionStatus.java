package gr.server.data.bet.enums;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;

/**
 * 
 * This is the status of a single {@link UserPrediction}, which belongs to a {@link UserBet}.
 * When a bet is placed this status is Pending.
 * Every x minutes a timer task might update the status, if the related event has finished.
 * 
 * @author liako
 *
 */
public enum PredictionStatus implements Serializable{
	
	@SerializedName("1")
	PENDING(1, "Pending"),
	
	@SerializedName("2")
	CORRECT(2, "Correct"),
	
	@SerializedName("3")
	MISSED(3, "Missed"),
	
	@SerializedName("4")
	WITHDRAWN(4, "Withdrawn");
	
	
	private static final long serialVersionUID = 1L;
	private String statusString;
	private int code;

	private PredictionStatus(int code, String statusString) {
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
