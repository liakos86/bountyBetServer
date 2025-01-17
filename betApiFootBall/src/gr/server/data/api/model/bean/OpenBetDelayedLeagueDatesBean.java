package gr.server.data.api.model.bean;

import java.util.HashSet;
import java.util.Set;

public class OpenBetDelayedLeagueDatesBean {
	
	Integer leagueId;
	
	Set<String> missingDates = new HashSet<>();

	public Integer getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(Integer leagueId) {
		this.leagueId = leagueId;
	}

	public Set<String> getMissingDates() {
		return missingDates;
	}

	public void setMissingDates(Set<String> missingDates) {
		this.missingDates = missingDates;
	}

	
	
}
