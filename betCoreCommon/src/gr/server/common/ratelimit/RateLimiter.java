package gr.server.common.ratelimit;

import java.util.concurrent.*;

public class RateLimiter {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;

    public RateLimiter(int requestsPerSecond) {
        this.semaphore = new Semaphore(requestsPerSecond); // Allow `requestsPerSecond` permits
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Refill the permits every second
        scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(requestsPerSecond - semaphore.availablePermits());
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Acquire a permit before making the API request.
     * Blocks the thread if no permits are available.
     */
    public void acquirePermit() throws InterruptedException {
        semaphore.acquire(); // Blocks until a permit is available
    }

    /**
     * Shuts down the rate limiter's scheduler gracefully.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
