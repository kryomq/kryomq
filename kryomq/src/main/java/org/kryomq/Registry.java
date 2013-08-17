package org.kryomq;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Registry<K, T> {
	protected Map<K, Set<T>> registry = new ConcurrentHashMap<K, Set<T>>();
	
	public void add(K topic, T subscriber) {
		synchronized(registry) {
			if(!registry.containsKey(topic))
				registry.put(topic, Collections.synchronizedSet(new HashSet<T>()));
		}
		Set<T> subs = registry.get(topic);
		subs.add(subscriber);
	}
	
	public Set<T> remove(K topic, T subscriber) {
		Set<T> subs = registry.get(topic);
		if(subs == null)
			return Collections.emptySet();
		subs.remove(subscriber);
		return subs;
	}
	
	public boolean has(K topic) {
		return registry.containsKey(topic);
	}
	
	public Set<T> get(K topic) {
		Set<T> subs = registry.get(topic);
		if(subs == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(subs);
	}
	
	public Set<T> remove(K topic) {
		return registry.remove(topic);
	}
	
	public void deregister(T subscriber) {
		synchronized(registry) {
			for(Set<T> subs : registry.values()) {
				subs.remove(subscriber);
			}
		}
	}

}
