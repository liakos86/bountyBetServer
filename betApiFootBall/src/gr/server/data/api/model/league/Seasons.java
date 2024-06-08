package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

/*
 * Json wrapper object.
 */
public class Seasons {
	
	List<Season> data;
	
	public Seasons() {
		data = new ArrayList<>();
	}
	
	public List<Season> getData() {
		return data;
	}

	public void setData(List<Season> data) {
		this.data = data;
	}
	
}
