package gr.server.data.api.model.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MatchEventStatistics")
public class MatchEventStatistics implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<MatchEventStatistic> data = new ArrayList<>();
	
	public MatchEventStatistics() {
		
	}

	public List<MatchEventStatistic> getData() {
		return data;
	}

	public void setData(List<MatchEventStatistic> data) {
		this.data = data;
	}

}
