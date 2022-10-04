package gr.server.data.live.helper;

import java.util.List;
import java.util.Map;

import gr.server.application.RestApplication;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Updates;
import gr.server.data.api.model.league.League;

public class LiveUpdatesHelper {

	public static void updateLiveDetails(Updates updates) {
		if (updates == null || updates.getData() == null || updates.getData().getData().isEmpty()) {
			System.out.println("NO DATA UPDATES");
			return;
		}
		
		List<MatchEvent> liveUpdates = updates.getData().getData();
		for (MatchEvent liveEvent : liveUpdates) {
			if (liveEvent.getLeague_id() == null || liveEvent.getId() == null) {
				continue;
			}
			
			// no entry for the league
			League league = new League(liveEvent.getLeague_id());
			Map<Integer, MatchEvent> leagueMap = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(league);
			if (leagueMap == null) {
				continue;
			}
			
			// no entry for the match
			MatchEvent matchEvent = leagueMap.get(liveEvent.getId());
			if (matchEvent == null) {
				continue;
			}
			
			checkHomeGoal(matchEvent, liveEvent);
			
			checkAwayGoal(matchEvent, liveEvent);
			
			getStatusForClient(matchEvent, liveEvent);
			
			if (matchEvent.isMarkedForRemoval()) {
				leagueMap.remove(matchEvent.getId());
				continue;
			}
			
			checkMarkForRemoval(matchEvent, liveEvent);
		
		}
	}

	private static void checkAwayGoal(MatchEvent matchEvent, MatchEvent liveEvent) {
		if (matchEvent.getAway_score() == null) {
			System.out.println("NO AWAY SCORE ********************** " + matchEvent.getAway_team().getName());
			return;
		}
		
		
		if (liveEvent.getAway_score() == null) {
			return;
		}
		
		if (matchEvent.awayGoalScored(liveEvent.getAway_score())) {
			matchEvent.setChangeEvent(ChangeEvent.AWAY_GOAL);
			System.out.println("GOOOOOAAAAAAAAAAL "  + matchEvent.getAway_team().getName());
			System.out.println("*****************************");
			System.out.println("*****************************");
			System.out.println("*****************************");
			System.out.println("*****************************");
		} else {
			matchEvent.setChangeEvent(ChangeEvent.NONE);
		}
		
		matchEvent.setAway_score(liveEvent.getAway_score());
	}

	private static void checkHomeGoal(MatchEvent matchEvent, MatchEvent liveEvent) {
		if (matchEvent.getHome_score() == null) {
			System.out.println("NO HOME SCORE ********************** "  + matchEvent.getHome_team().getName());
			return;
		}
		
		
		if (liveEvent.getHome_score() == null) {
			return;
		}

		if (matchEvent.homeGoalScored(liveEvent.getHome_score())) {
			matchEvent.setChangeEvent(ChangeEvent.HOME_GOAL);
			System.out.println("GOOOOOAAAAAAAAAAL "  + matchEvent.getHome_team().getName());
			System.out.println("*****************************");
			System.out.println("*****************************");
			System.out.println("*****************************");
			System.out.println("*****************************");
		} else {
			matchEvent.setChangeEvent(ChangeEvent.NONE);
		}
		
		matchEvent.setHome_score(liveEvent.getHome_score());
	}

	private static void checkMarkForRemoval(MatchEvent matchEvent, MatchEvent liveEvent) {
		if (liveEvent.getTime_live().toString().equalsIgnoreCase("Ended") 
				|| liveEvent.getStatus().equalsIgnoreCase("finished") 
				|| liveEvent.getStatus_loc().equalsIgnoreCase("finished")) {
			matchEvent.setMarkedForRemoval(true);
		}
	}

	private static void getStatusForClient(MatchEvent matchEvent, MatchEvent liveEvent) {
		Object time_live = liveEvent.getTime_live();
		if (time_live == null) {
			return;
		}
		
		String time_live_str = time_live.toString().replace(" ", "");
		if ("1sthalf".equalsIgnoreCase(time_live_str) || "2ndhalf".equalsIgnoreCase(time_live_str)) {
			return;
		}
		
		matchEvent.setStatus_for_client(time_live.toString());
		matchEvent.setStatus_loc(liveEvent.getStatus_loc());
		matchEvent.setStatus(liveEvent.getStatus());
		
	}

}
