package gr.server.data.api.model.events.transients;

import java.util.Map;

import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.Score;

public class ChangeEventSoccer {
	
	int eventId;
	
	ChangeEvent changeEvent;
	
	Score homeScore;
	
	Score awayScore;
	
	public ChangeEventSoccer(Map<String, Object> props) {
		this.eventId = (int) props.get("eventId");
		this.changeEvent = (ChangeEvent) props.get("changeEvent");
		this.homeScore = (Score) props.get("homeScore");
		this.awayScore = (Score) props.get("awayScore");
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public ChangeEvent getChangeEvent() {
		return changeEvent;
	}

	public void setChangeEvent(ChangeEvent changeEvent) {
		this.changeEvent = changeEvent;
	}

	public Score getHomeScore() {
		return homeScore;
	}

	public void setHomeScore(Score homeScore) {
		this.homeScore = homeScore;
	}

	public Score getAwayScore() {
		return awayScore;
	}

	public void setAwayScore(Score awayScore) {
		this.awayScore = awayScore;
	}

}
