package gr.server.data.api.model.events;

import java.util.ArrayList;
import java.util.List;

public class LiveUpdates {
	
	List<MatchEvent> data = new ArrayList<>();

	public List<MatchEvent> getData() {
		return data;
	}

	public void setData(List<MatchEvent> data) {
		this.data = data;
	}

}
