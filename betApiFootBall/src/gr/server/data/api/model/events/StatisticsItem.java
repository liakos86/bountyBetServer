package gr.server.data.api.model.events;

import java.util.HashMap;
import java.util.Map;

public class StatisticsItem {
	
	
	Map<String, String> items = new HashMap<>();

	public Map<String, String> getItems() {
		return items;
	}

	public void setItems(Map<String, String> items) {
		this.items = items;
	}

	
}
