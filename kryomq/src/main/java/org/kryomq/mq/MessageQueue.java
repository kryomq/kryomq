package org.kryomq.mq;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

/**
 * A byte-array backed queue for {@link Message} objects
 * @author robin
 *
 */
public class MessageQueue {
	/**
	 * The {@link Kryo} used for serializing/deserializing the {@link Message}s to/from bytes
	 */
	protected Kryo kryo;
	/**
	 * The {@link ByteArrayStreamPair} backing this {@link MessageQueue}
	 */
	protected ByteArrayStreamPair pair;
	/**
	 * The {@link Output} to write {@link Message}s to
	 */
	protected Output output;
	/**
	 * The {@link Input} to read {@link Message}s from
	 */
	protected Input input;
	
	/**
	 * Create a new {@link MessageQueue}
	 */
	public MessageQueue() {
		kryo = new MqKryo();
		pair = new ByteArrayStreamPair();
		output = new Output(pair.getOutputStream());
		input = new Input(pair.getInputStream(),1);
	}
	
	/**
	 * Put a {@link Message} into the queue, serializing to byte representation.
	 * @param message
	 */
	public void put(Message message) {
		kryo.writeObject(output, message);
		output.flush();
	}
	
	/**
	 * Take a {@link Message} from the queue, deserializing from byte representation
	 * @return
	 */
	public Message take() {
		return kryo.readObject(input, Message.class);
	}
	
	/**
	 * Return whether there are any available messages
	 * @return
	 */
	public boolean available() {
		return pair.available();
	}
}
