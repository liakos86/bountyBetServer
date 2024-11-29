package gr.server.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.stream.Collectors;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class TimerTaskHelper {

	public static TimerTask maintainWebSocketTask(SportScoreWebSocketClient client) {
		return new TimerTask() {
			public void run() {
				client.sendMessage(SportScoreApiConstants.SOCKET_KEEP_ALIVE_MSG);
			}
		};
	}

	/**
	 * We fetch all the events for today from sportscore.
	 * We settle the mongo event (not betslip) predictions which relate to the finished events.
	 * 
	 */
//	public static TimerTask settleFinishedEvents() {
//		return new TimerTask() {
//			public void run() {
//				new MongoTransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
//						System.out.println("Settle predictions Working in thread: " + Thread.currentThread().getName());
//
//						Set<MatchEvent> todaysFinishedEvents = new HashSet<>(); 
//						
//						List<LeagueWithData> todaysLeagues = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.get(0);
//						todaysLeagues.forEach(
//								leagueMatchesMap -> {
//									Set<MatchEvent> finishedEventsForLeague = leagueMatchesMap.getMatchEvents().stream().filter(
//											match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus()))
//									.collect(Collectors.toSet());
//									todaysFinishedEvents.addAll(finishedEventsForLeague);
//								});
//						
//						new MongoClientHelperImpl().settlePredictions(session, todaysFinishedEvents);
//					}
//				}.execute();
//			}
//		};
//	}

//	public static TimerTask settleOpenBets() {
//		return new TimerTask() {
//			public void run() {
//				new MongoTransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
//						System.out.println("Working in thread: " + Thread.currentThread().getName());
//						new MongoClientHelperImpl().settleOpenBets(session);
//					}
//				}.execute();
//			
//			}
//		};
//	}

}
