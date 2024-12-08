package gr.server.data.live.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import gr.server.application.RestApplication;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Score;
import gr.server.data.api.model.events.Updates;
import gr.server.data.enums.MatchEventPeriodStatus;
import gr.server.data.enums.MatchEventStatus;

public class LiveUpdatesHelper {
	
//	SoccerEventsTopicProducer topicProducer = RestApplication.SOCCER_EVENTS_TOPIC_PRODUCER;
	
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
		
		updateEventsAndPublishFirebaseTopicMessages(liveUpdates);
		
		
	}
	
	public void updateEventsAndPublishFirebaseTopicMessages(List<MatchEvent> liveUpdates) throws JMSException {
		for (MatchEvent liveEvent : liveUpdates) {
			MatchEvent relatedMatch = FootballApiCache.ALL_EVENTS.get(liveEvent.getId());
			if (relatedMatch == null) {
				continue;
			}
			
			
			boolean homeGoalScored = checkHomeGoal(relatedMatch, liveEvent);
			boolean awayGoalScored = checkAwayGoal(relatedMatch, liveEvent);
			//boolean matchStatusChanged = checkMatchStatus(relatedMatch, liveEvent);

			if (! (homeGoalScored || awayGoalScored )){
//					|| matchStatusChanged)) {
				relatedMatch.setChangeEvent(ChangeEvent.NONE.getChangeCode());
			}
			
//			System.out.println("Live: " + liveEvent);
//			System.out.println("Existing: " + relatedMatch);
//			System.out.println("*************************");
		}

	}
		
	private boolean checkAwayGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		if (matchEvent.getAway_score() == null) {
			matchEvent.setAway_score(new Score());
		}
		
		if (liveEvent.getAway_score() == null) {
			liveEvent.setAway_score(new Score());
			return false;
		}
		
		if (matchEvent.awayGoalScored(liveEvent.getAway_score())) {
			matchEvent.setAway_score(liveEvent.getAway_score());
			matchEvent.setChangeEvent(ChangeEvent.AWAY_GOAL.getChangeCode());
			produceTopicMessage(liveEvent, ChangeEvent.AWAY_GOAL);
			return true;
		} 
		
		return false;
	}

	private void produceTopicMessage(MatchEvent liveEvent, ChangeEvent event) {
		if (liveEvent.getHome_score() == null) {
			liveEvent.setHome_score(new Score());
		}
		
		if (liveEvent.getAway_score() == null) {
			liveEvent.setAway_score(new Score());
		}
		
		Map<String, String> msg = new HashMap<>();
		MatchEvent cached = FootballApiCache.ALL_EVENTS.get(liveEvent.getId());
		msg.put("eventId", liveEvent.getId().toString());
		msg.put("changeEvent", event.getChangeCode().toString());
		msg.put("homeScore", String.valueOf(liveEvent.getHome_score().getCurrent()));
		msg.put("awayScore", String.valueOf(liveEvent.getAway_score().getCurrent()));
		msg.put("homeTeam", cached.getHome_team().getName());
		msg.put("awayTeam", cached.getAway_team().getName());
		System.out.println("WILL SEND MESSAGE FOR " + liveEvent.toString());
//		RestApplication.sendTopicMessage(msg);
		RestApplication.sendFirebaseTopicMessage(msg);
	}

	private boolean checkHomeGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		
		if (matchEvent.getHome_score() == null) {
			matchEvent.setHome_score(new Score());
		}
		
		if (liveEvent.getHome_score() == null) {
			liveEvent.setHome_score(new Score());
			return false;
		}

		if (matchEvent.homeGoalScored(liveEvent.getHome_score())) {
			matchEvent.setHome_score(liveEvent.getHome_score());
			matchEvent.setChangeEvent(ChangeEvent.HOME_GOAL.getChangeCode());
			produceTopicMessage(matchEvent, ChangeEvent.HOME_GOAL);
			return true;
		}
		
		return false;
	}


	private boolean checkMatchStatus(MatchEvent relatedEvent, MatchEvent liveEvent) throws JMSException {
		boolean statusChange = false;
		
		String previousStatusTxt = relatedEvent.getStatus();
		MatchEventStatus previousStatus = MatchEventStatus.fromStatusText(previousStatusTxt);
		
		String previousStatusMoreTxt = relatedEvent.getStatus_more();
		MatchEventPeriodStatus previousStatusMore = MatchEventPeriodStatus.fromStatusMoreText(previousStatusMoreTxt);
		
		Object time_live = liveEvent.getTime_live();
		if (time_live == null) {
			System.out.println("***************** TIME LIVE NULL");
		}
		
		String time_live_str = time_live.toString();
		MatchEventPeriodStatus liveEventPeriodStatus = MatchEventPeriodStatus.fromStatusMoreText(time_live_str);
		if (MatchEventPeriodStatus.INPROGRESS_HALFTIME.equals(liveEventPeriodStatus)){
			if (MatchEventPeriodStatus.INPROGRESS_HALFTIME != previousStatusMore) {
				relatedEvent.setChangeEvent(ChangeEvent.HALF_TIME.getChangeCode());
				produceTopicMessage(relatedEvent, ChangeEvent.HALF_TIME);
				statusChange = true;
			}
			
			relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_HALFTIME.getStatusStr());
		}
		
		else if (MatchEventPeriodStatus.INPROGRESS_1ST_HALF.equals(liveEventPeriodStatus)) {
			
			if (previousStatusMore == null 
					|| previousStatusMore == MatchEventPeriodStatus.FINAL_RESULT_ONLY
					|| previousStatusMore == MatchEventPeriodStatus.EMPTY
					|| previousStatus == MatchEventStatus.NOTSTARTED) {
				relatedEvent.setChangeEvent(ChangeEvent.MATCH_START.getChangeCode());
				relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());//TODO
				produceTopicMessage(relatedEvent, ChangeEvent.MATCH_START);
				statusChange = true;
			}
			
			relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_1ST_HALF.getStatusStr());
		}
		
		else if (MatchEventPeriodStatus.INPROGRESS_2ND_HALF.equals(liveEventPeriodStatus)) {
	    	if (MatchEventPeriodStatus.INPROGRESS_HALFTIME == previousStatusMore) {
	    		
	    		//relatedEvent.setSecondHalfStart(current GMT millis); TODO
	    		
	    		relatedEvent.setChangeEvent(ChangeEvent.SECOND_HALF_START.getChangeCode());
	    		produceTopicMessage(relatedEvent, ChangeEvent.SECOND_HALF_START);
	    		statusChange = true;
	    	}
	    	
	    	relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_2ND_HALF.getStatusStr());
	    	
	    }

		else if (MatchEventStatus.FINISHED.equals(MatchEventStatus.fromStatusText(liveEvent.getStatus()))){
	    	if (MatchEventPeriodStatus.GAME_FINISHED != previousStatusMore) {
	    		relatedEvent.setChangeEvent(ChangeEvent.MATCH_END.getChangeCode());
	    		relatedEvent.setStatus(MatchEventStatus.FINISHED.getStatusStr());//TODO
	    		produceTopicMessage(relatedEvent, ChangeEvent.MATCH_END);
	    		statusChange = true;
	    	}
	    	
			relatedEvent.setStatus(MatchEventStatus.FINISHED.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.GAME_FINISHED.getStatusStr());
			
		}else if (liveEvent.getTime_live().equals("Awaiting extra time")) {
			relatedEvent.setChangeEvent(ChangeEvent.AWAITING_EXTRA_TIME.getChangeCode());
			produceTopicMessage(relatedEvent, ChangeEvent.AWAITING_EXTRA_TIME);
		}else {
			                  //TODO ******************* OOOOOOOOOOPS no change status for 2127403 --- inprogress ---- null --- Awaiting extra time
			System.out.println("******************* OOOOOOOOOOPS no change status for " + liveEvent +  " ---- " + liveEvent.getStatus_more() + " --- " + liveEvent.getTime_live());
		}
		
		return statusChange;
	}

}
