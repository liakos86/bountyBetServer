package gr.server.data.live.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gr.server.application.RestApplication;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Updates;
import gr.server.data.api.model.league.League;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.util.DateUtils;

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
				fixTheMissingLeague(liveEvent, league);
				leagueMap = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(league);
			}
			
			// no entry for the match
			MatchEvent matchEvent = leagueMap.get(liveEvent.getId());
			if (matchEvent == null) {
				continue;
			}
			
			checkHomeGoal(matchEvent, liveEvent);
			
			checkAwayGoal(matchEvent, liveEvent);
						
			getStatusForClient(matchEvent, liveEvent);
			
			checkMarkForRemoval(matchEvent, liveEvent);
			if (matchEvent.isMarkedForRemoval()) {
				leagueMap.remove(matchEvent.getId());
				continue;
			}
			
		
		}
	}
	
	private static void fixTheMissingLeague(MatchEvent liveEvent, League league) {
		Map<Integer, MatchEvent> leagueMap = new HashMap<>();
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = RestApplication.EVENTS_PER_DAY_PER_LEAGUE.get(DateUtils.todayStr());
		for ( Entry<League, Map<Integer, MatchEvent>> entry : todayLeaguesWithEvents.entrySet()) {
			if (entry.getKey() == null) {
				continue;
			}
			
			if (entry.getKey().getId() == liveEvent.getLeague_id()) {
				league = entry.getKey();
			}
		}
		
		Map<Integer, MatchEvent> leagueEvents = todayLeaguesWithEvents.get(league);
		MatchEvent matchEvent = leagueEvents.get(liveEvent.getId());
		leagueMap.put(liveEvent.getId(), matchEvent);
		RestApplication.LIVE_EVENTS_PER_LEAGUE.put(league, leagueMap);
		System.out.println("********* LEAGUE " + league + " WAS ADDED ***************");
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

	public static void getStatusForClient(MatchEvent matchEvent, MatchEvent liveEvent) {
		Object time_live = liveEvent.getTime_live();
		if (time_live == null) {
			return;
		}
		
		String time_live_str = time_live.toString().replace(" ", "");
		if ("1sthalf".equalsIgnoreCase(time_live_str) 
				|| "2ndhalf".equalsIgnoreCase(time_live_str) ) {
			return;
		}
		
		if ("inprogress".equalsIgnoreCase(time_live_str) && matchEvent.getStatus_for_client() != null) {
			System.out.println("INPROGRESS " + matchEvent.getHome_team().getName());
			return;
		}
		
		if ("inprogress".equalsIgnoreCase(time_live_str) && matchEvent.getStatus_for_client() == null) {
			try {
				Date matchDateTime = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT).parse(matchEvent.getStart_at());
				time_live = (int)  (new Date().getTime() - matchDateTime.getTime() / 60);
			} catch (ParseException e) {
				return;
			}
		}
		
		try{
			int parseInt = ((Double) time_live).intValue();
			time_live_str = parseInt + "'";
		}catch (Exception e) {
			try {
				int parseInt = Integer.parseInt(time_live_str);
				time_live_str = parseInt + "'";
			}catch(Exception e2) {
				//return?
			}
		}
		
		matchEvent.setStatus_for_client(time_live_str);
		matchEvent.setStatus_loc(liveEvent.getStatus_loc());
		matchEvent.setStatus(liveEvent.getStatus());
		
	}

}
