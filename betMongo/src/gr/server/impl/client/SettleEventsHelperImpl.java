package gr.server.impl.client;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.ClientSession;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.user.model.objects.SettledEvent;

public class SettleEventsHelperImpl {
	
	public List<SettledEvent> settleEvents(ClientSession session, List<MatchEvent> allEvents){
		List<SettledEvent> settled = new ArrayList<>();
//		for (League league : allLeagues) {
//			for (Event event : league.getEvents()) {
//				if (!event.getMatchStatus().equals("Finished")) {
//					continue;
//				}
//				
//				SettledEvent settledEvent = new SettledEvent();
//				settledEvent.setEventId(event.getMatchId());
//				int homeTeamScore = Integer.parseInt(event.getMatchHometeamScore());
//				int awayTeamScore = Integer.parseInt(event.getMatchAwayteamScore());
//				if (homeTeamScore > awayTeamScore) {
//					settledEvent.getSuccessfulPredictions().add(PredictionType.HOME_WIN);
//				}else if (homeTeamScore < awayTeamScore) {
//					settledEvent.getSuccessfulPredictions().add(PredictionType.AWAY_WIN);
//				}else {
//					settledEvent.getSuccessfulPredictions().add(PredictionType.DRAW);
//				}
//				
//				settled.add(settledEvent);
//			}
//		}
		
		return settled;
		
	}

}
