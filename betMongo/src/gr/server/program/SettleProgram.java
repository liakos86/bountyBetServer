package gr.server.program;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gr.server.data.api.model.events.MatchEvent;
//import gr.server.data.api.model.events.helper.ApiDataFetchHelper;
import gr.server.data.enums.MatchEventStatus;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.logging.Mongo;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class SettleProgram {

	static Logger logger = Mongo.logger;
	
	public static void main(String[] args) {
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
		
//		Runnable predsTask = () -> { settlePredictions(); };
//		Runnable betsTask = () -> { settleBets(); };
//		
//		executor.scheduleAtFixedRate(predsTask, 0, 20*60, TimeUnit.SECONDS);
//		executor.scheduleAtFixedRate(betsTask, 60, 20*60, TimeUnit.SECONDS);
		settlePredictions();
	}

	
	static void settlePredictions() {

		List<MatchEvent> todaysMatches = new ArrayList<>();// ApiDataFetchHelper.eventsForDate(new Date());
		
		Set<MatchEvent> todaysFinishedEvents = todaysMatches.stream().filter(
							match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus()))
					.collect(Collectors.toSet());
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				int settled = new MongoClientHelperImpl().settlePredictions(session, todaysFinishedEvents);
				logger.log(Level.INFO, "Settled " + settled + " predictions");
			}
		}.execute();

	}

	static void settleBets() {
		new MongoTransactionalBlock() {
			@Override
			public void begin() throws Exception {
				System.out.println("Working in thread: " + Thread.currentThread().getName());
				int settled = new MongoClientHelperImpl().settleOpenBets(session);
				logger.log(Level.INFO, "Settled " + settled + " bets");
				
			}
		}.execute();
	}
}
