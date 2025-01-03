package gr.server.data.api.model.events;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatistics {
	
	
	List<PlayerSeasonStatistic> data = new ArrayList<>();

	public List<PlayerSeasonStatistic> getData() {
		return data;
	}

	public void setData(List<PlayerSeasonStatistic> data) {
		this.data = data;
	}


}
