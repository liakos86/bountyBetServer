package gr.server.data.bet.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public enum PredictionType implements Serializable{
	
	@SerializedName("1")
	HOME_WIN(PredictionCategory.FINAL_RESULT, 1),
	
	@SerializedName("2")
	DRAW(PredictionCategory.FINAL_RESULT, 2),
	
	@SerializedName("3")
	AWAY_WIN(PredictionCategory.FINAL_RESULT, 3);
	
	private static final long serialVersionUID = 1L;
	private int code;
	private PredictionCategory category;

	private PredictionType(PredictionCategory category, int code) {
		this.code = code;
		this.category = category;
	}
	
	public int getCode() {
		return code;
	}

	public PredictionCategory getCategory() {
		return category;
	}

	public static List<Integer> getPredictionTypeCodes(List<PredictionType> predictionTypes) {
		List<Integer> codes = new ArrayList<>();
		predictionTypes.forEach(predictionType -> codes.add(predictionType.getCode()));
		return codes;
	}
}
