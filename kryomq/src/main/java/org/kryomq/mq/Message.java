package org.kryomq.mq;

import java.io.ByteArrayOutputStream;
import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

/**
 * Base class for transport messages transported across KryoMQ.
 * Messages are treated agnostically; KryoMQ sees them only as byte arrays,
 * leaving higher-level serialization or deserialization to the user.
 * @author robin
 *
 */
public final class Message {
	/**
	 * Whether this message should be delivered reliably (TCP) or not (UDP)
	 */
	public boolean reliable;
	/**
	 * The topic to which this message is destined
	 */
	public String topic;
	/**
	 * The topic from which this message originated
	 */
	public String origin;
	/**
	 * The content of this message
	 */
	public byte[] buf;

	/**
	 * Create a blank {@link Message}
	 */
	public Message() {}
	
	/**
	 * Create a {@link Message} with a specified buffer and reliable transport
	 * @param buf
	 */
	public Message(byte[] buf) {
		this(buf, true);
	}

	/**
	 * Create a {@link Message} with a specified buffer and optionally reliable transport
	 * @param buf
	 * @param reliable
	 */
	public Message(byte[] buf, boolean reliable) {
		this(null, buf, reliable);
	}
	
	/**
	 * Create a {@link Message} with a specified destination and optionally reliable transport
	 * @param topic
	 * @param reliable
	 */
	public Message(String topic, boolean reliable) {
		this(topic, null, reliable);
	}
	
	/**
	 * Create a {@link Message} with a specified destination and buffer and reliable transport
	 * @param topic
	 * @param buf
	 */
	public Message(String topic, byte[] buf) {
		this(topic, buf, true);
	}
	
	/**
	 * Create a {@link Message} with a specified destination, buffer, and transport reliability
	 * @param topic
	 * @param buf
	 * @param reliable
	 */
	public Message(String topic, byte[] buf, boolean reliable) {
		this.topic = topic;
		this.buf = buf;
		this.reliable = reliable;
	}
	
	/**
	 * Create a new {@link Message} to respond to this one
	 * @return
	 */
	public Message createReply() {
		return new Message(origin, reliable);
	}
	
	/**
	 * Serialize the {@code value} using the argument {@link Kryo} and use
	 * the result as the buffer.
	 * @param kryo
	 * @param value
	 * @return
	 */
	public Message set(Kryo kryo, Object value) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Output output = new Output(buf);
		kryo.writeClassAndObject(output, value);
		output.close();
		this.buf = buf.toByteArray();
		return this;
	}
	
	/**
	 * Deserialize the buffer using the argument {@link Kryo}
	 * @param kryo
	 * @return
	 */
	public Object get(Kryo kryo) {
		return kryo.readClassAndObject(new Input(buf));
	}


}
