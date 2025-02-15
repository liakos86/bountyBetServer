package gr.server.common.ratelimit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class TestRateLimiter {
	
	@Test
	public void testRate() throws InterruptedException {
		RateLimiter rateLimiter = new RateLimiter(5);
		
		long start = System.currentTimeMillis();
				
		Runnable settleWithdrawnPredsRunnableOrchestrator = () -> { 
			
			try {
				rateLimiter.acquirePermit();
				System.out.println(Thread.currentThread().getName() + " - " + (System.currentTimeMillis() - start));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Thread.currentThread().interrupt();
	            System.err.println("Request interrupted: ");
			}
			};
			
		
		ScheduledExecutorService settleWithdrawnPredsRunnableOrchestratorTask = Executors.newScheduledThreadPool(10);
		settleWithdrawnPredsRunnableOrchestratorTask.scheduleAtFixedRate(settleWithdrawnPredsRunnableOrchestrator, 0, 100, TimeUnit.MILLISECONDS);
		
		Thread.sleep(2000);
	}

}
