package gr.server.util;

import java.util.Date;
import java.util.TimerTask;

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

//	public static TimerTask settleEventsTask() {
//		return new TimerTask() {
//			public void run() {
//				new TransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
//						List<SettledEvent> settledEvents = new SettleEventsHelperImpl().settleEvents(session,
//								new ArrayList<>(RestApplication.EVENTS_PER_DAY_PER_LEAGUE));
//						// new MongoClientHelperImpl().storeSettledEvents(session, settledEvents);
//					}
//				}.execute();
//			}
//		};
//	}

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

//	public static TimerTask retrieveTeamsTask() {
//		return new TimerTask() {
//			public void run() {
//				System.out.println(
//						"GETTING TEAMS on: " + new Date() + "n" + "Thread's name: " + Thread.currentThread().getName());
//
//				new TransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
////						RestApplication.TEAMS_FOOTBALL = SportScoreClient.getTeams().getData();
//						RestApplication.TEAMS_FOOTBALL = MockApiClient.getTeamsFromFile().getData();
//
//					}
//				}.execute();
//			}
//		};
//	}

	/**
	 * Strategy: Call this API once per day to receive all the games. Then start
	 * calling the {@link #retrieveLiveEventsTask()} every x seconds. Every matching
	 * eventId must be updated with the live data until it is over.
	 * 
	 * @return
	 */
//	public static TimerTask retrieveEventsTask() {
//		return new TimerTask() {
//			public void run() {
//				System.out.println("REFRESHING events change on: " + new Date() + "n" + "Thread's name: "
//						+ Thread.currentThread().getName());
//
//						try {
////							RestApplication.EVENTS = SportScoreClient.getEvents(false).getData();
//							RestApplication.EVENTS_PER_DAY_PER_LEAGUE = MockApiClient.getEventsFromFile("eventsToday").getData();
//						} catch (IOException | ParseException | InterruptedException | URISyntaxException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//						RestApplication.EVENTS_PER_DAY_PER_LEAGUE.forEach(e -> RestApplication.EVENTS_PER_ID.put(e.getId(), e));
//
//			}
//		};
//	}

//	public static TimerTask retrieveLiveEventsTask() {
//		return new TimerTask() {
//			public void run() {
//				System.out.println("REFRESHING events change on: " + new Date() + "n" + "Thread's name: "
//						+ Thread.currentThread().getName());
//
//				new TransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
//						List<MatchEvent> events = SportScoreClient.getEvents(true).getData();
//						for (MatchEvent matchEvent : events) {
//							League matchLeague = matchEvent.getLeague();
//							Map<Integer, MatchEvent> leagueEvents = RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague);
//							if (leagueEvents == null) {
//								RestApplication.LIVE_EVENTS_PER_LEAGUE.put(matchLeague, new HashMap<>());
//							}
//							
//							RestApplication.LIVE_EVENTS_PER_LEAGUE.get(matchLeague).put(matchEvent.getId(), matchEvent);
//							RestApplication.TODAY_EVENTS_PER_ID.put(matchEvent.getId(), matchEvent);
//						}
//						
////						MockApiClient.getEventsFromFile("eventsLive").getData().forEach(e-> RestApplication.LIVE_EVENTS.put(e.getId(), e)) ;
//
//					}
//				}.execute();
//			}
//		};
//	}

//	/**
//	 * This task will create an empty map for every league.
//	 * Later, when the live matches will be fetched, every match will be placed in the proper league's map.
//	 * 
//	 * @return
//	 */
//	public static TimerTask retrieveLeaguesTask() {
//		return new TimerTask() {
//			public void run() {
//				System.out.println("REFRESHING leagues change on: " + new Date() + "n" + "Thread's name: "
//						+ Thread.currentThread().getName());
//
//				new TransactionalBlock() {
//					@Override
//					public void begin() throws Exception {
////						RestApplication.LEAGUES = SportScoreClient.getLeagues().getData();
//						List<League> leagues = MockApiClient.getLeaguesFromFile().getData();
//						leagues.forEach(l -> RestApplication.LIVE_EVENTS_PER_LEAGUE.put(l, new HashMap<>()));
//					}
//				}.execute();
//			}
//		};
//	}

	public static TimerTask maintainWebSocketTask(SportScoreWebSocketClient client) {
		return new TimerTask() {
			public void run() {
				client.sendMessage("{\"event\":\"pusher:ping\",\"data\":{}}");
			}
		};
	}

//	public static TimerTask mockLiveEventsTask() {
//		return new TimerTask() {
//			public void run() {
//				if (RestApplication.LIVE_EVENTS.isEmpty()) {
//					return;
//				}
//				
//				List<MatchEvent> liveEvents = MockApiClient.getLiveEvents().getData();
//				liveEvents.forEach(ev -> RestApplication.LIVE_EVENTS.put(ev.getId(), ev));
//				
//			}
//		};
//	}

}
