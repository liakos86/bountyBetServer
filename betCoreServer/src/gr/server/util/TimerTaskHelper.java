package gr.server.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import javax.jms.JMSException;

import gr.server.application.RestApplication;
import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.transaction.helper.TransactionalBlock;

public class TimerTaskHelper {

	public static TimerTask deleteStaleEventsTask() {
		return new TimerTask() {
			public void run() {
				new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						// new MongoClientHelperImpl().deletePastEvents();
					}
				}.execute();
			}
		};
	}


	public static TimerTask deleteBountiesTask() {
		return new TimerTask() {
			public void run() {
				System.out.println("Deleting bounties for: " + new Date() + "n" + "Thread's name: "
						+ Thread.currentThread().getName());

				new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						// new MongoClientHelperImpl().deleteBountiesUntil(session,
						// DateUtils.getBountiesExpirationDate());
					}
				}.execute();
			}
		};
	}

	public static TimerTask settleBetsTask() {
		return new TimerTask() {
			public void run() {

				new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						// new MongoClientHelperImpl().settleBets(session, RestApplication.SETTLED);
					}
				}.execute();
			}
		};
	}

	public static TimerTask maintainWebSocketTask(SportScoreWebSocketClient client) {
		return new TimerTask() {
			public void run() {
				client.sendMessage("{\"event\":\"pusher:ping\",\"data\":{}}");
			}
		};
	}

	public static TimerTask liveMatchMinuteUpdateTimerTask() {
		return new TimerTask() {
			public void run() {
				RestApplication.MINUTE_TRACKER.refresh();
			}
		};
	}


	public static TimerTask sendTopicMessageTask() {
		return new TimerTask() {
			public void run() {
				Map<String, Object> mockMap = new HashMap<>();
				Map<Integer, MatchEvent> matchesOfFirstLeague = RestApplication.LIVE_EVENTS_PER_LEAGUE.entrySet().iterator().next().getValue();
				Entry<Integer, MatchEvent> matchEntry = matchesOfFirstLeague.entrySet().iterator().next();
				mockMap.put("eventId", matchEntry.getKey());
				mockMap.put("changeEvent", ChangeEvent.HOME_GOAL);
				mockMap.put("homeScore", matchEntry.getValue().getHome_score());
				mockMap.put("awayScore", matchEntry.getValue().getAway_score());
				try {
					RestApplication.SOCCER_EVENTS_TOPIC_PRODUCER.sendTopicMessage(mockMap);
				} catch (JMSException e) {
					System.out.println("*******ERROR JMS");
					e.printStackTrace();
				}
			}
		};
	}

}
