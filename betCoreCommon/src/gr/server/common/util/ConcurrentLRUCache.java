package gr.server.common.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentLRUCache<K, V> {

	private final int capacity;
	private final Map<K, V> cache;
	private final Object lock = new Object();

	public ConcurrentLRUCache(int capacity) {
		this.capacity = capacity;
		this.cache = new ConcurrentHashMap<>(capacity);

		// Create a LinkedHashMap with LRU eviction policy
		Map<K, V> lruMap = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > ConcurrentLRUCache.this.capacity;
			}
		};

		// Wrap the LinkedHashMap in a synchronized wrapper for thread safety
		this.cache.putAll(Collections.synchronizedMap(lruMap));
	}

	public void put(K key, V value) {
		synchronized (lock) {
			cache.put(key, value);
		}
	}

	public V get(K key) {
		synchronized (lock) {
			return cache.get(key);
		}
	}

	public boolean containsKey(K key) {
		synchronized (lock) {
			return cache.containsKey(key);
		}
	}

	public void remove(K key) {
		synchronized (lock) {
			cache.remove(key);
		}
	}

	public int size() {
		synchronized (lock) {
			return cache.size();
		}
	}

}
