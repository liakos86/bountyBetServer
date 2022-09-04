package gr.server.data.user.model.objects;

import java.util.ArrayList;
import java.util.List;

import gr.server.data.bet.enums.PredictionType;

public class SettledEvent {
	
	String eventId;
	
	List<PredictionType> successfulPredictions = new ArrayList<>();
	
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public List<PredictionType> getSuccessfulPredictions() {
		return successfulPredictions;
	}

	public void setSuccessfulPredictions(List<PredictionType> successfulPredictions) {
		this.successfulPredictions = successfulPredictions;
	}
	
	
}
