package gr.server.data.api.model.league;

import java.util.Map;

public class StandingRow implements Comparable<StandingRow>{
	
	int position;
	
	int points;
	
	int home_points;
	
	int away_points;
	
	Map<String, String> fields;
	Map<String, String> home_fields;
	Map<String, String> away_fields;
	Map<String, String> details;

	Team team;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getHome_points() {
		return home_points;
	}

	public void setHome_points(int home_points) {
		this.home_points = home_points;
	}

	public int getAway_points() {
		return away_points;
	}

	public void setAway_points(int away_points) {
		this.away_points = away_points;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

	public Map<String, String> getHome_fields() {
		return home_fields;
	}

	public void setHome_fields(Map<String, String> home_fields) {
		this.home_fields = home_fields;
	}

	public Map<String, String> getAway_fields() {
		return away_fields;
	}

	public void setAway_fields(Map<String, String> away_fields) {
		this.away_fields = away_fields;
	}

	public Map<String, String> getDetails() {
		return details;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	@Override
	public int compareTo(StandingRow o) {
		
		return (o.getAway_points() + o.getHome_points()) - (this.away_points + this.home_points);
	}
	
}
