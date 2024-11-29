package gr.server.program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
//import gr.server.data.api.model.events.helper.ApiDataFetchHelper;
import gr.server.data.enums.MatchEventStatus;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.logging.Mongo;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class SettleProgram {

	static Logger logger = Mongo.logger;
	
	public static void main(String[] args) throws Exception {
		
//		ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
		
//		Runnable predsTask = () -> { 
		//	settlePredictions(); 
//			};
		
//		Runnable betsTask = () -> { 
			settleBets(); 
//			};
		
//		executor.scheduleAtFixedRate(predsTask, 0, 20*60, TimeUnit.SECONDS);
//		executor.scheduleAtFixedRate(betsTask, 60, 20*60, TimeUnit.SECONDS);
	}

	
	private static void settlePredictions() throws Exception {
		logger.log(Level.ERROR, "Settling starts again");

		
		
		new MongoClientHelperImpl().settlePredictions(new HashSet<>());

	}

	private static void settleBets() throws Exception {
//		new MongoClientHelperImpl().settleOpenBets();
	}
}
