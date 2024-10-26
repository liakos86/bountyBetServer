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
    
}
