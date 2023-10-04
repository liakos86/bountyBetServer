package gr.server.data.api.model.league;

import java.util.List;

import gr.server.data.api.model.league.StandingRow;

public class Standing {
	
	int id;
	
	String slug;
	
	int round;
	
	List<StandingRow> standings_rows;

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
	
}
