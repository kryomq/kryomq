package org.kryomq.mq;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

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
public class BufferMessage {
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
	 * Create a blank {@link BufferMessage}
	 */
	public BufferMessage() {}
	
	/**
	 * Create a {@link BufferMessage} with a specified buffer and reliable transport
	 * @param buf
	 */
	public BufferMessage(byte[] buf) {
		this(buf, true);
	}

	/**
	 * Create a {@link BufferMessage} with a specified buffer and optionally reliable transport
	 * @param buf
	 * @param reliable
	 */
	public BufferMessage(byte[] buf, boolean reliable) {
		this(null, buf, reliable);
	}
	
	/**
	 * Create a {@link BufferMessage} with a specified destination and optionally reliable transport
	 * @param topic
	 * @param reliable
	 */
	public BufferMessage(String topic, boolean reliable) {
		this(topic, null, reliable);
	}
	
	/**
	 * Create a {@link BufferMessage} with a specified destination and buffer and reliable transport
	 * @param topic
	 * @param buf
	 */
	public BufferMessage(String topic, byte[] buf) {
		this(topic, buf, true);
	}
	
	/**
	 * Create a {@link BufferMessage} with a specified destination, buffer, and transport reliability
	 * @param topic
	 * @param buf
	 * @param reliable
	 */
	public BufferMessage(String topic, byte[] buf, boolean reliable) {
		this.topic = topic;
		this.buf = buf;
		this.reliable = reliable;
	}
	
	/**
	 * Create a new {@link BufferMessage} to respond to this one
	 * @return
	 */
	public BufferMessage createReply() {
		return new BufferMessage(origin, reliable);
	}
	
	/**
	 * Serialize the {@code value} using the argument {@link Kryo} and use
	 * the result as the buffer.
	 * @param kryo
	 * @param value
	 * @return
	 */
	public BufferMessage set(Kryo kryo, Object value) {
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
