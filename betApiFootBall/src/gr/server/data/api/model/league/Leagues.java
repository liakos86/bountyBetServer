package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

public class Leagues {
	
	List<League> data;
	
	public Leagues(){
		data = new ArrayList<League>();
	}

	public List<League> getData() {
		return data;
	}

	public void setData(List<League> data) {
		this.data = data;
	}
	
}
