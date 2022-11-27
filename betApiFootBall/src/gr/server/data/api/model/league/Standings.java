package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

public class Standings {
	
	List<Standing> data;
	
	public Standings() {
		data = new ArrayList<>();
	}

	public List<Standing> getData() {
		return data;
	}

	public void setData(List<Standing> data) {
		this.data = data;
	}
	
}
