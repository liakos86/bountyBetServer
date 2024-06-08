package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

public class StandingTables {
	
	List<StandingTable> data = new ArrayList<>();

	public List<StandingTable> getData() {
		return data;
	}

	public void setData(List<StandingTable> data) {
		this.data = data;
	}

	
}
