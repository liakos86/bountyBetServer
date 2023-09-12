package gr.server.util;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.websocket.SportScoreWebSocketClient;
import gr.server.data.global.helper.mock.MockApiDataFetchHelper;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.mongo.util.SyncHelper;
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

	public static TimerTask maintainWebSocketTask(SportScoreWebSocketClient client) {
		return new TimerTask() {
			public void run() {
				client.sendMessage("{\"event\":\"pusher:ping\",\"data\":{}}");
			}
		};
	}

	/**
	 * We fetch all the events for today from sportscore.
	 * We settle the mongo event (not betslip) predictions which relate to the finished events.
	 * 
	 */
	public static TimerTask settleFinishedEvents() {
		return new TimerTask() {
			public void run() {
				new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						System.out.println("Working in thread: " + Thread.currentThread().getName());
//						List<MatchEvent> todayEvents = ApiDataFetchHelper.eventsForDate(new Date());
						List<MatchEvent> todayEvents = MockApiDataFetchHelper.fetchEvents("finishedEvents").getData();
						new MongoClientHelperImpl().settlePredictions(session, todayEvents);
					}
				}.execute();
			}
		};
	}

	public static TimerTask settleOpenBets() {
		return new TimerTask() {
			public void run() {
				new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						System.out.println("Working in thread: " + Thread.currentThread().getName());
						SyncHelper.settleOpenBets(session);
						//new MongoClientHelperImpl().settleBets(session);
					}
				}.execute();
			
			}
		};
	}

}
