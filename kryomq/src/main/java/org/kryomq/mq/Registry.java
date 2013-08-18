package org.kryomq.mq;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registration/subscription map.  Rather than a single value, each key is associated with
 * a {@link Set} of values.
 * @author robin
 *
 * @param <K>
 * @param <T>
 */
public class Registry<K, T> {
	/**
	 * The main registry map
	 */
	protected Map<K, Set<T>> registry = new ConcurrentHashMap<K, Set<T>>();
	
	/**
	 * Add the argument subscriber to this registry under the argument key
	 * @param topic The key to add the subscriber to
	 * @param subscriber The subscriber to add
	 */
	public void add(K topic, T subscriber) {
		synchronized(registry) {
			if(!registry.containsKey(topic))
				registry.put(topic, Collections.synchronizedSet(new HashSet<T>()));
		}
		Set<T> subs = registry.get(topic);
		subs.add(subscriber);
	}
	
	/**
	 * Remove the argument subscriber from this registry under the argument key
	 * @param topic The key to remove the subscriber from
	 * @param subscriber The subscriber to remove
	 * @return
	 */
	public Set<T> remove(K topic, T subscriber) {
		Set<T> subs = registry.get(topic);
		if(subs == null)
			return Collections.emptySet();
		subs.remove(subscriber);
		return subs;
	}
	
	/**
	 * Returns whether there are any subscribers for the argument topic
	 * @param topic The topic to check for subscribers
	 * @return {@code true} if there are any subscribers
	 */
	public boolean has(K topic) {
		return registry.containsKey(topic) && get(topic).size() > 0;
	}
	
	/**
	 * Returns all the subscribers for the argument topic.  The returned {@link Set}
	 * is unmodifiable.
	 * @param topic The topic whose subscribers are to be retrieved
	 * @return The current subscribers
	 */
	public Set<T> get(K topic) {
		Set<T> subs = registry.get(topic);
		if(subs == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(subs);
	}
	
	/**
	 * Remove all subscribers for the argument topic.
	 * @param topic The topic to remove subscribers from
	 * @return The subscribers that were removed
	 */
	public Set<T> remove(K topic) {
		return registry.remove(topic);
	}
	
	/**
	 * Remove a subscriber from all topics
	 * @param subscriber The subscriber to remove
	 */
	public void deregister(T subscriber) {
		synchronized(registry) {
			for(Set<T> subs : registry.values()) {
				subs.remove(subscriber);
			}
		}
	}

}
