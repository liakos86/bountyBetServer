package gr.server.program;

import java.util.HashSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gr.server.common.logging.CommonLogger;
import gr.server.impl.client.MongoClientHelperImpl;

public class SettleProgram {

	static Logger logger = CommonLogger.logger;
	
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
