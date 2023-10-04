package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

import gr.server.data.api.model.league.Section;

public class Sections {
	
	List<Section> data;
	
	public Sections(){
		data = new ArrayList<Section>();
	}

	public List<Section> getData() {
		return data;
	}

	public void setData(List<Section> data) {
		this.data = data;
	}
	
}
