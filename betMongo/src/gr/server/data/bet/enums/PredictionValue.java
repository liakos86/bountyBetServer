package gr.server.data.bet.enums;

public enum PredictionValue {
	
	HOME(1, "Home team"),
	
	DRAW(2, "Draw"),
	
	AWAY(3, "Away team");
	
	private int code;
	private String predictionString;

	private PredictionValue(int code, String predictionString) {
		this.code = code;
		this.predictionString = predictionString;
	}
	
	public String getPredictionString() {
		return predictionString;
	}



	public void setPredictionString(String predictionString) {
		this.predictionString = predictionString;
	}



	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	
}
