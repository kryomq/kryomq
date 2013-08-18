package org.kryomq.mq;

/**
 * An object which sends byte arrays to a topic.
 * @author robin
 *
 */
public interface ByteArraySender {
	/**
	 * Send the byte array
	 * @param buf
	 */
	public void send(byte[] buf);
	/**
	 * Close this byte array sender.
	 */
	public void close();
}
