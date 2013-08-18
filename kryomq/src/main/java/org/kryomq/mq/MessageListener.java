package org.kryomq.mq;

/**
 * A listener for data messages sent over KryoMQ
 * @author robin
 *
 */
public interface MessageListener {
	/**
	 * Called when a {@link Message} is received
	 * @param message The {@link Message} that was received
	 */
	public void messageReceived(Message message);
}
