package gr.server.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import gr.server.application.RestApplication;
import gr.server.data.constants.Server;
import gr.server.data.user.model.objects.SettledEvent;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.client.SettleEventsHelperImpl;
import gr.server.impl.client.SportScoreClient;
import gr.server.transaction.helper.TransactionalBlock;

public class TimerTaskHelper {
	
	public static TimerTask getMonthChangeCheckerTask(){
		return new TimerTask() {
	        public void run() {
//	            System.out.println("Checking month change on: " + new Date() + "n" +
//	              "Thread's name: " + Thread.currentThread().getName());
	            
	            if (!DateUtils.isFirstDayOfMonth()){
	            	return;
	            }
	            
	            String monthToSettle = DateUtils.getPastMonthAsString(1);
	           	try {
					//new MongoClientHelperImpl().settleMonthlyAward(monthToSettle);
				} catch (Exception e) {
					e.printStackTrace();
					Server.LOG.debug("Rollback for monthly award");
				}
	        }
	    };
	}
	
	
	public static TimerTask deleteStaleEventsTask(){
		return new TimerTask() {
	        public void run() {
	        	new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						//new MongoClientHelperImpl().deletePastEvents();
					}
				}.execute();
	        }
	    };
	}
	
	public static TimerTask settleEventsTask(){
		return new TimerTask() {
	        public void run() {
	            new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						List<SettledEvent> settledEvents = new SettleEventsHelperImpl().settleEvents(session, new ArrayList<>(RestApplication.EVENTS));
						//new MongoClientHelperImpl().storeSettledEvents(session, settledEvents);
					}
				}.execute();
	        }
	    };
	}

	public static TimerTask deleteBountiesTask() {
		return new TimerTask() {
	        public void run() {
	            System.out.println("Deleting bounties for: " + new Date() + "n" +
	              "Thread's name: " + Thread.currentThread().getName());

	            new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						//new MongoClientHelperImpl().deleteBountiesUntil(session, DateUtils.getBountiesExpirationDate());
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
						//new MongoClientHelperImpl().settleBets(session, RestApplication.SETTLED);
					}
				}.execute();
	        }
	    };
	}


	public static TimerTask retrieveTeamsTask() {
		return new TimerTask() {
	        public void run() {
	            System.out.println("GETTING TEAMS on: " + new Date() + "n" +
	              "Thread's name: " + Thread.currentThread().getName());

	            new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						RestApplication.TEAMS_FOOTBALL = SportScoreClient.getTeams().getData();
					}
				}.execute();
	        }
	    };
	}
	
	public static TimerTask retrieveEventsTask(){
		return new TimerTask() {
	        public void run() {
	            System.out.println("REFRESHING events change on: " + new Date() + "n" +
	              "Thread's name: " + Thread.currentThread().getName());

	            new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						RestApplication.EVENTS = SportScoreClient.getEvents().getData();
					}
				}.execute();
	        }
	    };
	}

}
