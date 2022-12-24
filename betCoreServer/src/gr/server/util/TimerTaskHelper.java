package gr.server.util;

import java.util.Date;
import java.util.TimerTask;

import gr.server.application.RestApplication;
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

}
