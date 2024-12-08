package gr.server.bets.settle.impl;

import java.util.HashSet;
import java.util.Set;

import gr.server.bets.settle.def.TaskHandler;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.impl.client.MongoClientHelperImpl;

public class UserBetPredictionHandler implements TaskHandler<MatchEvent>, Runnable {
	
	public static final int NUM_WORKERS = 4; // Number of worker threads
	private int batch;// = 10;

	public UserBetPredictionHandler(int batch) {
		this.batch = batch;
	}

	@Override
	public boolean handle(Set<MatchEvent> toHandle) throws Exception {
//		System.out.println(Thread.currentThread().getName() +  " HANDLING "+ toHandle.size());
		
		if (toHandle.isEmpty()) {
			return true;
		}
		
		try {
			return new MongoClientHelperImpl().settlePredictions(toHandle);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public void run() {
		
//		System.out.println(Thread.currentThread().getName() + " WILL HANDLE " + batch);
		
		Set<MatchEvent> batchObjects = new HashSet<>(batch);

        // Collect 10 UserPredictions before processing
        for (int i = 0; i < batch; i++) {
            MatchEvent finishedEventId;
			try {
				
				finishedEventId = FootballApiCache.FINISHED_EVENTS.take();
				batchObjects.add(finishedEventId);
			} catch (InterruptedException e) {
				reEnqueueMatches(batchObjects);
                Thread.currentThread().interrupt();  // Preserve the interrupt flag
				e.printStackTrace();
				return;  
			}  // Blocks if the queue is empty
        }

        // Process the batch of 10 UserPredictions
        try {
			boolean handled = handle(batchObjects);
			if (!handled) {
				reEnqueueMatches(batchObjects);
			}
			
		} catch (Exception e) {
			reEnqueueMatches(batchObjects);
		}
		
	}
	
	
	void reEnqueueMatches(Set<MatchEvent> batchObjects) {
		for (MatchEvent eventId : batchObjects) {
			try {
				FootballApiCache.FINISHED_EVENTS.put(eventId);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  // Re-add events back to the queue
        }
	}
	
	
}
