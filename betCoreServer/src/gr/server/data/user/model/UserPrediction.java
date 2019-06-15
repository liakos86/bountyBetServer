package gr.server.data.user.model;

import java.io.Serializable;


public class UserPrediction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String eventId;
	
	int prediction;
	
	int predictionStatus;
	
	Double oddValue;
	
	String predictionDescription;
	
	
	public String getPredictionDescription() {
		return predictionDescription;
	}

	public void setPredictionDescription(String predictionDescription) {
		this.predictionDescription = predictionDescription;
	}

	public Double getOddValue() {
		return oddValue;
	}

	public void setOddValue(Double oddValue) {
		this.oddValue = oddValue;
	}

	public String getEventId() {
		return eventId;
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	
	
	public int getPredictionStatus() {
		return predictionStatus;
	}

	public void setPredictionStatus(int predictionStatus) {
		this.predictionStatus = predictionStatus;
	}

	public int getPrediction() {
		return prediction;
	}

	public void setPrediction(int prediction) {
		this.prediction = prediction;
	}

	public static void copyFields(UserPrediction sourcePrediction, UserPrediction destinationPrediction) {
		destinationPrediction.setOddValue(sourcePrediction.getOddValue());
		destinationPrediction.setPrediction(sourcePrediction.getPrediction());
		destinationPrediction.setPredictionStatus(sourcePrediction.getPredictionStatus());
	}

}
