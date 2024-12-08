package gr.server.data.api.model.league;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gr.server.data.api.model.events.MatchEvent;

/**
 * Sent via rest calls to MyBetOddsService.
 * 
 * @author liako
 *
 */
public class LeagueWithData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int leagueId;
		
	List<MatchEvent> matchEvents;
	
	public LeagueWithData() {
		matchEvents = new ArrayList<>();
	}
	
	public int getLeagueId() {
		return leagueId;
	}

	public void setLeagueId(int leagueId) {
		this.leagueId = leagueId;
	}

	public List<MatchEvent> getMatchEvents() {
		return matchEvents;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LeagueWithData)) {
			return false;
		}
		
		LeagueWithData other = (LeagueWithData) obj;
		if (this.leagueId == 0 || other.leagueId == 0) {
			throw new RuntimeException("League with zero id: " + this.leagueId + " to " + other.leagueId);
		}
		
		return this.leagueId == (other.leagueId);
	}
	
	@Override
	public int hashCode() {
		if (leagueId > 0)
		return 37 * leagueId ;
		
		
		return 37 * matchEvents.size();
	}
    
}
