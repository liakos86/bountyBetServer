package gr.server.data.api.model.events;

import java.io.Serializable;
import java.util.List;

import gr.server.data.api.model.league.Season;

public class PlayerSeasonStatistic implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer id;
	Integer player_id;
	Integer season_id;
	Double rating;
	Season season;

	Player player;
	List<PlayerStatisticData> details;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPlayer_id() {
		return player_id;
	}

	public void setPlayer_id(Integer player_id) {
		this.player_id = player_id;
	}

	public Integer getSeason_id() {
		return season_id;
	}

	public void setSeason_id(Integer season_id) {
		this.season_id = season_id;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Season getSeason() {
		return season;
	}

	public void setSeason(Season season) {
		this.season = season;
	}

	public List<PlayerStatisticData> getDetails() {
		return details;
	}

	public void setDetails(List<PlayerStatisticData> details) {
		this.details = details;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlayerSeasonStatistic)) {
			return false;
		}
		
		PlayerSeasonStatistic other = (PlayerSeasonStatistic) obj;
		return this.player.id == other.player.id;
	}
	
	@Override
	public int hashCode() {
		return this.player.id * 33;
	}
	
	
	
}
