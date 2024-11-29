package gr.server.data.api.model.events;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MatchEventIncidentsWithStatistics")
public class MatchEventIncidentsWithStatistics implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int eventId;
	
	MatchEventIncidents matchEventIncidents;
	
	MatchEventStatistics matchEventStatistics;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public MatchEventIncidents getMatchEventIncidents() {
		return matchEventIncidents;
	}

	public void setMatchEventIncidents(MatchEventIncidents matchEventIncidents) {
		this.matchEventIncidents = matchEventIncidents;
	}

	public MatchEventStatistics getMatchEventStatistics() {
		return matchEventStatistics;
	}

	public void setMatchEventStatistics(MatchEventStatistics matchEventStatistics) {
		this.matchEventStatistics = matchEventStatistics;
	}
	
}
