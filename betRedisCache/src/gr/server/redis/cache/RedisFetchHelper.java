package gr.server.redis.cache;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisFetchHelper {
	
	public static void fetchCachedEventsIntoLeagues() {
		
		JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

		  // Get the pool and use the database
		  try (Jedis jedis = jedisPool.getResource()) {

			  String value = jedis.get("mykey");
			  jedis.set("mykey", "Hello from Jedis");
			  System.out.println( value );
	
			  jedis.zadd("vehicles", 0, "car");
			  jedis.zadd("vehicles", 0, "bike");
			  Set<String> vehicles = jedis.zrange("vehicles", 0, -1);
			  System.out.println( vehicles );
		  }
		
	}

}
