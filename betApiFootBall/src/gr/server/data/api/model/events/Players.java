package gr.server.data.api.model.events;

import java.util.ArrayList;
import java.util.List;

public class Players {
	
	List<Player> data = new ArrayList<>();

	public List<Player> getData() {
		return data;
	}

	public void setData(List<Player> data) {
		this.data = data;
	}
	
	
	
}
