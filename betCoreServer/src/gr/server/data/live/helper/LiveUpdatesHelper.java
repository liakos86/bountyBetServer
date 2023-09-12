package gr.server.data.live.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import gr.server.application.RestApplication;
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
		for (MatchEvent liveEvent : liveUpdates) {
			MatchEvent relatedMatch = RestApplication.ALL_EVENTS.get(liveEvent.getId());
			
			checkHomeGoal(relatedMatch, liveEvent);
			checkAwayGoal(relatedMatch, liveEvent);
			checkMatchStatus(relatedMatch, liveEvent);
		}
	}
		
	private void checkAwayGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		if (matchEvent.getAway_score() == null) {
			matchEvent.setAway_score(new Score());
		}
		
		if (liveEvent.getAway_score() == null) {
			return;
		}
		
		if (matchEvent.awayGoalScored(liveEvent.getAway_score())) {
			matchEvent.setAway_score(liveEvent.getAway_score());
			produceTopicMessage(liveEvent, ChangeEvent.AWAY_GOAL);
		} 
		
	}

	private void produceTopicMessage(MatchEvent liveEvent, ChangeEvent event) {
		Map<String, Object> msg = new HashMap<>();
		msg.put("eventId", liveEvent.getId());
		msg.put("changeEvent", event);
		msg.put("homeScore", liveEvent.getHome_score());
		msg.put("awayScore", liveEvent.getAway_score());
		RestApplication.sendTopicMessage(msg);
	}

	private void checkHomeGoal(MatchEvent matchEvent, MatchEvent liveEvent) throws JMSException {
		if (matchEvent.getHome_score() == null) {
			matchEvent.setHome_score(new Score());
		}
		
		if (liveEvent.getHome_score() == null) {
			return;
		}

		if (matchEvent.homeGoalScored(liveEvent.getHome_score())) {
			matchEvent.setHome_score(liveEvent.getHome_score());
			produceTopicMessage(matchEvent, ChangeEvent.HOME_GOAL);
		}
		
	}


	public void checkMatchStatus(MatchEvent relatedEvent, MatchEvent liveEvent) throws JMSException {
		String previousStatusTxt = relatedEvent.getStatus();
		MatchEventStatus previousStatus = MatchEventStatus.fromStatusText(previousStatusTxt);
		
		String previousStatusMoreTxt = relatedEvent.getStatus_more();
		MatchEventPeriodStatus previousStatusMore = MatchEventPeriodStatus.fromStatusMoreText(previousStatusMoreTxt);
		
		Object time_live = liveEvent.getTime_live();
		if (time_live == null) {
			System.out.println("***************** TIME LIVE NULL");
			return;
		}
		
		String time_live_str = time_live.toString();
		MatchEventPeriodStatus liveEventPeriodStatus = MatchEventPeriodStatus.fromStatusMoreText(time_live_str);
		if (MatchEventPeriodStatus.INPROGRESS_HALFTIME.equals(liveEventPeriodStatus)){
			if (MatchEventPeriodStatus.INPROGRESS_HALFTIME != previousStatusMore) {
				produceTopicMessage(relatedEvent, ChangeEvent.HALF_TIME);
			}
			
			relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_HALFTIME.getStatusStr());
		}
		
		else if (MatchEventPeriodStatus.INPROGRESS_1ST_HALF.equals(liveEventPeriodStatus)) {
			
			if (previousStatusMore == null 
					|| previousStatusMore == MatchEventPeriodStatus.FINAL_RESULT_ONLY
					|| previousStatusMore == MatchEventPeriodStatus.EMPTY
					|| previousStatus == MatchEventStatus.NOTSTARTED) {
				System.out.println("MATCH START " + relatedEvent.getHome_team().getName());
				produceTopicMessage(relatedEvent, ChangeEvent.MATCH_START);
			}
			
			relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_1ST_HALF.getStatusStr());
		}
		
		else if (MatchEventPeriodStatus.INPROGRESS_2ND_HALF.equals(liveEventPeriodStatus)) {
	    	if (MatchEventPeriodStatus.INPROGRESS_HALFTIME == previousStatusMore) {
	    		produceTopicMessage(relatedEvent, ChangeEvent.SECOND_HALF_START);
	    	}
	    	
	    	relatedEvent.setStatus(MatchEventStatus.INPROGRESS.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.INPROGRESS_2ND_HALF.getStatusStr());
	    	
	    }

		else if (MatchEventStatus.FINISHED.equals(MatchEventStatus.fromStatusText(liveEvent.getStatus()))){
	    	if (MatchEventPeriodStatus.GAME_FINISHED != previousStatusMore) {
	    		produceTopicMessage(relatedEvent, ChangeEvent.MATCH_END);
	    	}
	    	
			relatedEvent.setStatus(MatchEventStatus.FINISHED.getStatusStr());
			relatedEvent.setStatus_more(MatchEventPeriodStatus.GAME_FINISHED.getStatusStr());
			
		}else {
			System.out.println("******************* OOOOOOOOOOPS no change status for " + liveEvent.getId() + " --- " + liveEvent.getStatus() + " ---- " + liveEvent.getStatus_more() + " --- " + liveEvent.getTime_live());
		}
		
	}

}
