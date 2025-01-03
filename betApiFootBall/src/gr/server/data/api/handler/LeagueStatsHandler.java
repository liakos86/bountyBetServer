package gr.server.data.api.handler;

import java.util.Set;


import gr.server.data.api.model.league.League;
import gr.server.handle.def.TaskHandler;

public class LeagueStatsHandler implements TaskHandler<League>, Runnable {

	public static final int NUM_WORKERS = 10;
	private Set<League> leagues;
	
	public LeagueStatsHandler(Set<League> leagues) {
		this.leagues = leagues;
	}

	@Override
	public boolean handle(Set<League> toHandle) {
		
		try {
			return true;// new ApiDataFetchHelper().fetchLeagueStandings(toHandle);
		} catch (Exception e) {
			return false;
		}
		
	}

	@Override
	public void run() {
		
		handle(leagues);
		
	}
	
	

}
