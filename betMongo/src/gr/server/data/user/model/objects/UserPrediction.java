package gr.server.data.user.model.objects;

import java.io.Serializable;

import gr.server.data.api.model.league.Team;
import gr.server.data.bet.enums.PredictionCategory;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.bet.enums.PredictionType;


public class UserPrediction 
implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String mongoId;

	String mongoBetId;

	int eventId;
	
	int homeTeamId;
	
	int awayTeamId;
	
	int sportId;
	
	Team homeTeam;
	
	Team awayTeam;
	
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

	public Team getHomeTeam() {
		return homeTeam;
	}

	public void setHomeTeam(Team homeTeam) {
		this.homeTeam = homeTeam;
	}

	public Team getAwayTeam() {
		return awayTeam;
	}

	public void setAwayTeam(Team awayTeam) {
		this.awayTeam = awayTeam;
	}

	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public String getMongoBetId() {
		return mongoBetId;
	}

	public void setMongoBetId(String mongoBetId) {
		this.mongoBetId = mongoBetId;
	}

	public int getHomeTeamId() {
		return homeTeamId;
	}

	public void setHomeTeamId(int homeTeamId) {
		this.homeTeamId = homeTeamId;
	}

	public int getAwayTeamId() {
		return awayTeamId;
	}

	public void setAwayTeamId(int awayTeamId) {
		this.awayTeamId = awayTeamId;
	}

	public int getSportId() {
		return sportId;
	}

	public void setSportId(int sportId) {
		this.sportId = sportId;
	}
	
}
