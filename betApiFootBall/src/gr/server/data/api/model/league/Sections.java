package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;

public class Sections {
	
	List<Section> data;
	
	String access_token;
	
	public Sections(){
		data = new ArrayList<Section>();
	}

	public List<Section> getData() {
		return data;
	}

	public void setData(List<Section> data) {
		this.data = data;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	
}
