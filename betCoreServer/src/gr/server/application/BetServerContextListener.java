package gr.server.application;

import gr.server.util.TimerTaskHelper;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class BetServerContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SHUTTING DOWN");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		System.out.println("GETTING TEAMS");
		TimerTask retrieveTeamsTimerTask = TimerTaskHelper.retrieveTeamsTask();
		Timer retrieveTeamsTimer = new Timer("retrieveTeamsTimer");
		retrieveTeamsTimer.schedule(retrieveTeamsTimerTask, 2000);

//		TimerTask deleteStaleEventsTask = TimerTaskHelper.deleteStaleEventsTask();
//		Timer deleteEventsTimer = new Timer("deleteEventsTimer");
//		deleteEventsTimer.schedule(deleteStaleEventsTask, 0);
//
		System.out.println("SCHEDULING");
		TimerTask refreshEventsTimerTask = TimerTaskHelper.retrieveEventsTask();
		Timer refreshEventsTimer = new Timer("retrieveEventsTimer");
		refreshEventsTimer.schedule(refreshEventsTimerTask, 7000);
//
//		System.out.println("SETTLING EVENTS");
//		TimerTask settleEventsTimerTask = TimerTaskHelper.settleEventsTask();
//		Timer settleEventsTimer = new Timer("settleEventsTimer");
//		settleEventsTimer.schedule(settleEventsTimerTask, 15000);
//
//		System.out.println("SETTLING EVENTS");
//		TimerTask settleBetsTimerTask = TimerTaskHelper.settleBetsTask();
//		Timer settleBetsTimer = new Timer("settleBetsTimer");
//		settleBetsTimer.schedule(settleBetsTimerTask, 20000);


	}

}
