package gr.server.data.api.model.events;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatistics {
	
	List<PlayerStatistic> data = new ArrayList<>();

	public List<PlayerStatistic> getData() {
		return data;
	}

	public void setData(List<PlayerStatistic> data) {
		this.data = data;
	}

	
	
}
