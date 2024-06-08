package gr.server.data.api.model.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MatchEventIncidents")
public class MatchEventIncidents implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<MatchEventIncident> data = new ArrayList<>();

	public List<MatchEventIncident> getData() {
		return data;
	}

	public void setData(List<MatchEventIncident> data) {
		this.data = data;
	}

}
