package gr.server.data.bet.enums;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum PredictionCategory implements Serializable{
	
	@SerializedName("1")
	FINAL_RESULT(1),
	
	@SerializedName("2")
	OVER_UNDER(2);
	
	private static final long serialVersionUID = 1L;
	private int categoryCode;

	private PredictionCategory(int categoryCode) {
		this.categoryCode = categoryCode;
	}

	public int getCategoryCode() {
		return categoryCode;
	}
	
	static PredictionCategory ofCode(int code) {
		for (PredictionCategory pc : PredictionCategory.values()) {
			if (code == pc.categoryCode) {
				return pc;
			}
		}
		
		return null;
	}

}
