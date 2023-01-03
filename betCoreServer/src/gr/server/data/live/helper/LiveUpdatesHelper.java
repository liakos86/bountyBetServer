package gr.server.data.live.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import gr.server.application.RestApplication;
import gr.server.application.SoccerEventsTopicProducer;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Score;
import gr.server.data.api.model.events.Updates;
import gr.server.data.api.model.league.League;
import gr.server.data.enums.MatchEventPeriodStatus;
import gr.server.data.enums.MatchEventStatus;
import gr.server.util.DateUtils;

public class LiveUpdatesHelper {
	
	SoccerEventsTopicProducer topicProducer = RestApplication.SOCCER_EVENTS_TOPIC_PRODUCER;
	
	/**
	 * Multiple match updates arrive here.
	 * The reference match might belong to a league that has no live events yet.
	 * We retrieve the league in that case.
	 * Also the reference match might not be in the live matches of the league.
	 * TODO: we need to fetch the match and then put it in the list of the live matches of the league.
	 * 
	 * @param updates
	 * @throws JMSException 
	 */
	public void updateLiveDetails(Updates updates) throws JMSException {
		if (updates == null || updates.getData() == null || updates.getData().getData().isEmpty()) {
			System.out.println("NO DATA UPDATES");
			return;
		}
		
		List<MatchEvent> liveUpdates = updates.getData().getData();
		for (MatchEvent liveEvent : liveUpdates) {
			createLiveEntryForUnknownLeague(liveEvent);
			createLiveEntryForTheMissingLeague(liveEvent);
			createLiveEntryForTheMissingMatch(liveEvent);
			
			League league = new League(liveEvent.getLeague_id());
			Map<Integer, MatchEvent> leagueMap = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(league);
			MatchEvent matchEvent = leagueMap.get(liveEvent.getId());
			if (matchEvent == null) {// no live details yet for the match
				System.out.println("*************** NO MATCH!!! ************");
			}
			
			checkHomeGoal(matchEvent, liveEvent);
			checkAwayGoal(matchEvent, liveEvent);
			updateStatus(matchEvent, liveEvent);
			checkMarkForRemoval(matchEvent, liveEvent);
			if (matchEvent.isMarkedForRemoval()) {
				leagueMap.remove(matchEvent.getId());
				continue;
			}
		
		}
	}
	
	/**
	 * If the input live event belongs to a league that is missing from the {@link RestApplication#LIVE_EVENTS_PER_LEAGUE}.
	 * We find the actual league from the {@link RestApplication#EVENTS_PER_DAY_PER_LEAGUE},
	 * 
	 * @param liveEvent
	 * @param league
	 */
	private void createLiveEntryForTheMissingLeague(MatchEvent liveEvent) {
		League league = new League(liveEvent.getLeague_id());
		if (RestApplication.LIVE_EVENTS_PER_LEAGUE.get(league) != null) {//live events exist for the league
			return;
		}
		
		Map<Integer, MatchEvent> leagueMap = new HashMap<>();
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = RestApplication.EVENTS_PER_DAY_PER_LEAGUE.get(DateUtils.todayStr());
		
		int id = league.getId();
		league = todayLeaguesWithEvents.keySet().stream().filter(l->l.getId()==id).collect(Collectors.toList()).get(0);
		
		Map<Integer, MatchEvent> leagueEvents = todayLeaguesWithEvents.get(league);
		MatchEvent matchEvent = leagueEvents.get(liveEvent.getId());
		leagueMap.put(liveEvent.getId(), matchEvent);
		RestApplication.LIVE_EVENTS_PER_LEAGUE.put(league, leagueMap);
	}
	
	/**
	 * If the input live event has no league id,
	 * We find the actual league from the {@link RestApplication#EVENTS_PER_DAY_PER_LEAGUE}, 
	 * searching with the event id.
	 * 
	 * @param liveEvent
	 */
	private void createLiveEntryForUnknownLeague(MatchEvent liveEvent) {
		if (liveEvent.getLeague_id() != null) {
			return;
		}
		
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = RestApplication.EVENTS_PER_DAY_PER_LEAGUE.get(DateUtils.todayStr());
		
		League unknownLeague = null;
		for(Entry<League, Map<Integer, MatchEvent>> todayLeagueEntry : todayLeaguesWithEvents.entrySet()) {
			Map<Integer, MatchEvent> todayLeagueEvents = todayLeagueEntry.getValue();
			MatchEvent matchEvent = todayLeagueEvents.get(liveEvent.getId());
			if (matchEvent != null) {
				unknownLeague = todayLeagueEntry.getKey();
				liveEvent.setLeague_id(unknownLeague.getId());
				liveEvent.setLeague(unknownLeague);
				break;
			}
		}
		
		RestApplication.LIVE_EVENTS_PER_LEAGUE.put(unknownLeague, new HashMap<>());
		System.out.println("********* LEAGUE " + unknownLeague + " WAS ADDED ***************");
	}
	
	/**
	 * If the input live event belongs to a league that is missing from the {@link RestApplication#LIVE_EVENTS_PER_LEAGUE}.
	 * We find the actual league from the {@link RestApplication#EVENTS_PER_DAY_PER_LEAGUE},
	 *  
	 * 
	 * @param liveEvent
	 * @param league
	 * @throws JMSException 
	 */
	private void createLiveEntryForTheMissingMatch(MatchEvent liveEvent) throws JMSException {
		League league = new League(liveEvent.getLeague_id());
		Map<Integer, MatchEvent> leagueMap = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(league);
		MatchEvent matchEvent = leagueMap.get(liveEvent.getId());
		if (matchEvent != null) {//live details exist for the match
			return;
		}
		
		produceTopicMessage(liveEvent, ChangeEvent.MATCH_START);
		
		Map<League, Map<Integer, MatchEvent>> todayLeaguesWithEvents = RestApplication.EVENTS_PER_DAY_PER_LEAGUE.get(DateUtils.todayStr());
		Map<Integer, MatchEvent> leagueEvents = todayLeaguesWithEvents.get(league);
		liveEvent = leagueEvents.get(liveEvent.getId());
		liveEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
		liveEvent.setMain_odds(null);
		leagueMap.put(liveEvent.getId(), liveEvent);
		RestApplication.MINUTE_TRACKER.track(liveEvent);
		System.out.println("********* TRACKING " + liveEvent + " ***************");
	}

	private void checkAwayGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		if (matchEvent.getAway_score() == null) {
			matchEvent.setAway_score(new Score());
		}
		
		if (liveEvent.getAway_score() == null) {
			return;
		}
		
		if (matchEvent.awayGoalScored(liveEvent.getAway_score())) {
			matchEvent.setChangeEvent(ChangeEvent.AWAY_GOAL);
			produceTopicMessage(liveEvent, ChangeEvent.AWAY_GOAL);
		} else {
			matchEvent.setChangeEvent(ChangeEvent.NONE);
		}
		
		matchEvent.setAway_score(liveEvent.getAway_score());
	}

	private void produceTopicMessage(MatchEvent liveEvent, ChangeEvent event) throws JMSException {
		Map<String, Object> msg = new HashMap<>();
		msg.put("eventId", liveEvent.getId());
		msg.put("changeEvent", event);
		msg.put("homeScore", liveEvent.getHome_score());
		msg.put("awayScore", liveEvent.getAway_score());
		topicProducer.sendTopicMessage(msg);
	}

	private void checkHomeGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		if (matchEvent.getHome_score() == null) {
			matchEvent.setHome_score(new Score());
		}
		
		if (liveEvent.getHome_score() == null) {
			return;
		}

		if (matchEvent.homeGoalScored(liveEvent.getHome_score())) {
			matchEvent.setChangeEvent(ChangeEvent.HOME_GOAL);
			produceTopicMessage(liveEvent, ChangeEvent.HOME_GOAL);
		} else {
			matchEvent.setChangeEvent(ChangeEvent.NONE);
		}
		
		matchEvent.setHome_score(liveEvent.getHome_score());
	}

	private static void checkMarkForRemoval(MatchEvent matchEvent, MatchEvent liveEvent) {
		if (liveEvent.getTime_live().toString().equalsIgnoreCase("Ended") 
				|| liveEvent.getStatus().equalsIgnoreCase("finished") ) {
//				|| liveEvent.getStatus_loc().equalsIgnoreCase("finished")) {
			matchEvent.setMarkedForRemoval(true);
			RestApplication.MINUTE_TRACKER.discard(liveEvent);
		}
	}

	public static void updateStatus(MatchEvent matchEvent, MatchEvent liveEvent) {
//		System.out.println();
//		System.out.print(liveEvent.getId() + " ** LIVE EVENT HAS: time live " + liveEvent.getTime_live());
//		System.out.print( " *** : time det " + liveEvent.getTime_details());
//		System.out.print( " *** : status " + liveEvent.getStatus());
//		System.out.print(" *** : status more " + liveEvent.getStatus_more());
		
		matchEvent.setStatus(liveEvent.getStatus());//remove?
		
		Object time_live = liveEvent.getTime_live();
		if (time_live == null) {
			return;
		}
		
		String time_live_str = time_live.toString();
		MatchEventPeriodStatus liveEventPeriodStatus = MatchEventPeriodStatus.fromStatusMoreText(time_live_str);
		if (MatchEventPeriodStatus.INPROGRESS_HALFTIME.equals(liveEventPeriodStatus)
				|| MatchEventPeriodStatus.INPROGRESS_1ST_HALF.equals(liveEventPeriodStatus)
					|| MatchEventPeriodStatus.INPROGRESS_2ND_HALF.equals(liveEventPeriodStatus)) {
			matchEvent.setStatus_more(liveEventPeriodStatus.getStatusStr());
			matchEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			return;
		}
		
		if (MatchEventStatus.FINISHED.equals(MatchEventStatus.fromStatusText(liveEvent.getStatus()))){
			matchEvent.setStatus(MatchEventStatus.FINISHED.getStatusStr());
			matchEvent.setStatus_more(MatchEventPeriodStatus.GAME_FINISHED.getStatusStr());
			return;
		}
		
	}

}
