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
public enum PredictionSettleStatus implements Serializable{
	
	@SerializedName("1")
	UNSETTLED(1, "Unsettled"),
	
	@SerializedName("2")
	SETTLED(2, "Settled");
	
	private static final long serialVersionUID = 1L;
	private String statusString;
	private int code;

	private PredictionSettleStatus(int code, String statusString) {
		this.setCode(code);
		this.setStatusString(statusString);
	}
	
	public PredictionSettleStatus fromCode(int code) throws Exception {
		for(PredictionSettleStatus status : PredictionSettleStatus.values()){
			if (code == status.getCode()){
				return status;
			}
		}
		throw new Exception("PredictionSettle code: "+code+" not found");
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
