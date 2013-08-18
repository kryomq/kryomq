package org.kryomq.mq;

/**
 * An object which receives byte arrays from a topic and can distribute them.
 * @author robin
 *
 */
public interface ByteArrayReceiver {
	/**
	 * Take the oldest byte array off the queue and return it.
	 * Block until there is a byte array to take if the queue is empty.
	 * @return
	 */
	public byte[] receive();
	/**
	 * Returns whether there are any byte arrays to take.
	 * @return
	 */
	public boolean available();
	/**
	 * Close this receiver
	 */
	public void close();
}
