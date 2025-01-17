package gr.server.bets.settle.impl;

import java.util.HashSet;
import java.util.Set;

import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.handle.def.TaskHandler;
import gr.server.impl.client.MongoClientHelperImpl;

public class UserBetWithdrawnPredictionHandler implements TaskHandler<MatchEvent>, Runnable {
	
	public static final int NUM_WORKERS = 1; // Number of worker threads
	private int batch;// = 10;

	public UserBetWithdrawnPredictionHandler(int batch) {
		this.batch = batch;
	}

	@Override
	public boolean handle(Set<MatchEvent> toHandle) throws Exception {
		
		if (toHandle.isEmpty()) {
			return true;
		}
		
		try {
			return new MongoClientHelperImpl().settleWithdrawnPredictions(toHandle);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public void run() {
		
		
		Set<MatchEvent> batchObjects = new HashSet<>(batch);

        for (int i = 0; i < batch; i++) {
            MatchEvent finishedEventId;
			try {
				
				finishedEventId = FootballApiCache.WITHDRAWN_EVENTS.take();
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
        	//batchObjects.stream().map(MatchEvent::getId).toList();
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
				FootballApiCache.WITHDRAWN_EVENTS.put(eventId);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  // Re-add events back to the queue
        }
	}
	
	
}
