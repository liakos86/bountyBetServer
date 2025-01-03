package gr.server.data.api.model.league;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.server.data.api.model.events.PlayerSeasonStatistic;

public class StandingTable {
	
	int id;
	
	String slug;
	
	int round;
	
	Map<String, String> total_keys; 
	
	Map<String, String> home_keys; 
	
	Map<String, String> away_keys; 
	
	List<StandingRow> standings_rows;
		
	//added by me
	Set<PlayerSeasonStatistic> season_player_statistics = new HashSet<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public List<StandingRow> getStandings_rows() {
		return standings_rows;
	}

	public void setStandings_rows(List<StandingRow> standings_rows) {
		this.standings_rows = standings_rows;
	}

	public Map<String, String> getTotal_keys() {
		return total_keys;
	}

	public void setTotal_keys(Map<String, String> total_keys) {
		this.total_keys = total_keys;
	}

	public Map<String, String> getHome_keys() {
		return home_keys;
	}

	public void setHome_keys(Map<String, String> home_keys) {
		this.home_keys = home_keys;
	}

	public Map<String, String> getAway_keys() {
		return away_keys;
	}

	public void setAway_keys(Map<String, String> away_keys) {
		this.away_keys = away_keys;
	}

	public Set<PlayerSeasonStatistic> getSeason_player_statistics() {
		return season_player_statistics;
	}
	
	
	
}
