package org.kryomq.mq;
/**
 * Interface through which higher-level objects than raw byte arrays may be
 * received from an {@link MqClient}
 * @author robin
 *
 * @param <T>
 */
public interface Receiver<T> {
	/**
	 * Receive the next object
	 * @return
	 */
	public T receive();
	/**
	 * Returns whether any object is available
	 * @return
	 */
	public boolean available();
	/**
	 * Close this receiver
	 */
	public void close();
}
