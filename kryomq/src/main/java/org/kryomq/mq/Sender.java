package org.kryomq.mq;

/**
 * Interface through which higher-level objects may be sent to an {@link MqClient}
 * @author robin
 *
 * @param <T>
 */
public interface Sender<T> {
	/**
	 * Send an object
	 * @param object
	 */
	public void send(T object);
	/**
	 * Close this sender
	 */
	public void close();
}
