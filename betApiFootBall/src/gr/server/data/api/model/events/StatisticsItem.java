package gr.server.data.api.model.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;

public class StatisticsItem {
	
	
	Map<String, String> items = new HashMap<>();

	public Map<String, String> getItems() {
		return items;
	}

	public void setItems(Map<String, String> items) {
		this.items = items;
	}

	
}
