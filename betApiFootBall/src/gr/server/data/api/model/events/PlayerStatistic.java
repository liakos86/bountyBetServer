package gr.server.data.api.model.events;

import java.util.List;

import gr.server.data.api.model.league.Season;

public class PlayerStatistic {
	
	Integer id;
	Integer player_id;
	Integer season_id;
	Double rating;
	Season season;

	List<PlayerStatisticData> details;
	
	
}
