package gr.server.application;

import gr.server.util.TimerTaskHelper;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class BetServerContextListener
implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SHUTTING DOWN");
	}
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
			TimerTask monthChangeCheckerTask = TimerTaskHelper.getMonthChangeCheckerTask();
		    Timer timer = new Timer("CheckMonthChangeTimer");
		   // timer.scheduleAtFixedRate(monthChangeCheckerTask, DateUtils.getTomorrowMidnight(),  ServerConstants.DAILY_INTERVAL);
	
		    TimerTask deleteStaleEventsTask = TimerTaskHelper.deleteStaleEventsTask();
		    Timer deleteEventsTimer = new Timer("deleteEventsTimer");
		    deleteEventsTimer.schedule(deleteStaleEventsTask, 0);
		    
		    TimerTask refreshEventsTimerTask = TimerTaskHelper.refreshEventsTask();
		    Timer refreshEventsTimer = new Timer("refreshEventsTimer");
		    refreshEventsTimer.schedule(refreshEventsTimerTask, 0);
		    
		    
		    TimerTask deleteInvalidBounties = TimerTaskHelper.deleteBountiesTask();
		    Timer deleteInvalidBountiesTimer = new Timer("deleteInvalidBountiesTimer");
		   // refreshEventsTimer.schedule(refreshEventsTimerTask, 0);
		    
		
	}

}
