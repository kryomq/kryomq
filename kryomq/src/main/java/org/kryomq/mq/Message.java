package org.kryomq.mq;

import org.kryomq.kryo.Kryo;

/**
 * A message to be transported by KryoMQ.
 * @author robin
 *
 */
public final class Message extends BufferMessage {

	/**
	 * Create a blank {@link Message}
	 */
	public Message() {
	}

	/**
	 * Create an empty {@link Message} with the argument destination and reliability
	 * @param topic
	 * @param reliable
	 */
	public Message(String topic, boolean reliable) {
		super(topic, reliable);
	}

	/**
	 * Create a {@link Message} with the argument destination, buffer, and reliability
	 * @param topic
	 * @param buf
	 * @param reliable
	 */
	public Message(String topic, byte[] buf, boolean reliable) {
		super(topic, buf, reliable);
	}

	@Override
	public Message createReply() {
		return new Message(origin, reliable);
	}
	
	@Override
	public Message set(Kryo kryo, Object value) {
		return (Message) super.set(kryo, value);
	}
}
