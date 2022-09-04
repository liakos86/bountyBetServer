package gr.server.data.user.model.objects;

import java.io.Serializable;

import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.bet.enums.PredictionType;


public class UserPrediction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int eventId;
	
	PredictionCategory predictionCategory;
	
	PredictionType predictionType;
	
	PredictionStatus predictionStatus;
	
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

	public int getEventId() {
		return eventId;
	}
	
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public PredictionStatus getPredictionStatus() {
		return predictionStatus;
	}

	public void setPredictionStatus(PredictionStatus predictionStatus) {
		this.predictionStatus = predictionStatus;
	}

	public PredictionType getPredictionType() {
		return predictionType;
	}

	public void setPredictionType(PredictionType predictionType) {
		this.predictionType = predictionType;
	}

	public PredictionCategory getPredictionCategory() {
		return predictionCategory;
	}

	public void setPredictionCategory(PredictionCategory predictionCategory) {
		this.predictionCategory = predictionCategory;
	}
	
	

//	public static void copyFields(UserPrediction sourcePrediction, UserPrediction destinationPrediction) {
//		destinationPrediction.setOddValue(sourcePrediction.getOddValue());
//		destinationPrediction.setPrediction(sourcePrediction.getPrediction());
//		destinationPrediction.setPredictionStatus(sourcePrediction.getPredictionStatus());
//	}

}
