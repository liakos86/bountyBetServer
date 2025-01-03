package gr.server.data.api.model.league;

import java.io.Serializable;
import java.util.Map;

public class StandingRow implements Comparable<StandingRow>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int position;
	
	int points;
	
	String home_points;
	
	String away_points;
	
	int home_position;
	
	int away_position;
	
//	String goals_total;
//	String matches_total;//:38
//	String wins_total;//:19
//	String draws_total;//:11
//	String losses_total;//:8
	
	Map<String, String> fields; 
//	goals_total:"61:37"
//	points_total:68
//	percentage_total:null
//	streak_total:null
	
	Map<String, String> home_fields;
//	matches_home:19
//	wins_home:12
//	draws_home:6
//	losses_home:1
//	goals_home:"42:18"
//	points_home:42
//	percentage_home:null
//	streak_home:null
	
	Map<String, String> away_fields;
//	matches_away:19
//	wins_away:7
//	draws_away:5
//	losses_away:7
//	goals_away:"19:19"
//	points_away:26
//	percentage_away:null
//	streak_away:null
	
	
	Map<String, String> details; // "name" key indicates what ticket this position gives

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

	public String getHome_points() {
		return home_points;
	}

	public void setHome_points(String home_points) {
		this.home_points = home_points;
	}

	public String getAway_points() {
		return away_points;
	}

	public void setAway_points(String away_points) {
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
	
	

	public int getHome_position() {
		return home_position;
	}

	public void setHome_position(int home_position) {
		this.home_position = home_position;
	}

	public int getAway_position() {
		return away_position;
	}

	public void setAway_position(int away_position) {
		this.away_position = away_position;
	}

	
	
	
//	public String getGoals_total() {
//		return goals_total;
//	}
//
//	public void setGoals_total(String goals_total) {
//		this.goals_total = goals_total;
//	}
//
//	public String getMatches_total() {
//		return matches_total;
//	}
//
//	public void setMatches_total(String matches_total) {
//		this.matches_total = matches_total;
//	}
//
//	public String getWins_total() {
//		return wins_total;
//	}
//
//	public void setWins_total(String wins_total) {
//		this.wins_total = wins_total;
//	}
//
//	public String getDraws_total() {
//		return draws_total;
//	}
//
//	public void setDraws_total(String draws_total) {
//		this.draws_total = draws_total;
//	}
//
//	public String getLosses_total() {
//		return losses_total;
//	}
//
//	public void setLosses_total(String losses_total) {
//		this.losses_total = losses_total;
//	}

	@Override
	public int compareTo(StandingRow o) {
		if (this.getPosition() > o.getPosition()) {
			return -1;
		}
		
		return 1;
	}
	
}
