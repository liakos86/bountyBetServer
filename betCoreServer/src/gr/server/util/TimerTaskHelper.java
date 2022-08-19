package gr.server.util;

import gr.server.data.api.model.league.League;
import gr.server.data.constants.Server;
import gr.server.impl.client.ApiFootballClient;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.transaction.helper.TransactionalBlock;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

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
					new MongoClientHelperImpl().settleMonthlyAward(monthToSettle);
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
						new MongoClientHelperImpl().deletePastEvents();
					}
				}.execute();
	        }
	    };
	}
	
	public static TimerTask refreshEventsTask(){
		return new TimerTask() {
	        public void run() {
	            System.out.println("REFRESHING events change on: " + new Date() + "n" +
	              "Thread's name: " + Thread.currentThread().getName());

	            new TransactionalBlock() {
					@Override
					public void begin() throws Exception {
						List<League> leagues = ApiFootballClient.getLeagues();
						new MongoClientHelperImpl().storeLeagues(session, leagues);
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
						new MongoClientHelperImpl().deleteBountiesUntil(session, DateUtils.getBountiesExpirationDate());
					}
				}.execute();
	        }
	    };
	}

}
