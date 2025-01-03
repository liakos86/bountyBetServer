package gr.server.application;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitService {

    private static final ConcurrentHashMap<String, RequestTracker> tokenBucketMap = new ConcurrentHashMap<>();

    
    private static final long LIMIT_PERIOD = TimeUnit.MINUTES.toMillis(1); // 1 minute

    
    // Maximum requests per minute
    private static final int REQUESTS_PER_MINUTE = 100;


    // Create a new rate limit bucket with 60 requests per minute
    private RequestTracker getBucket(String token) {
    	
    	long currentTime = System.currentTimeMillis();
    	RequestTracker tracker = tokenBucketMap.get(token);

         if (tracker == null || currentTime - tracker.timestamp > LIMIT_PERIOD) {
    	
//        Refill refill = Refill.greedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
    	 tracker = new RequestTracker(currentTime, 0);
    	tokenBucketMap.put(token, tracker);
         }
         
         return tracker;
    }

    // Check if a token exceeds the rate limit
    public boolean isRateLimitExceeded(String token) {
        RequestTracker tracker = getBucket(token);
        if (tracker.requestCount >= REQUESTS_PER_MINUTE) {
            return true; // Token has exceeded the limit
        }

        // Increment request count
        tracker.requestCount++;
        return false;
    }
    
    
    private static class RequestTracker {
        long timestamp;
        int requestCount;

        RequestTracker(long timestamp, int requestCount) {
            this.timestamp = timestamp;
            this.requestCount = requestCount;
        }
    }
}
