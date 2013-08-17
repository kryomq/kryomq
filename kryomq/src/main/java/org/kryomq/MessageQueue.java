package org.kryomq;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;
import org.kryomq.util.ByteArrayStreamPair;

public class MessageQueue {
	protected Kryo kryo;
	protected ByteArrayStreamPair pair;
	protected Output output;
	protected Input input;
	
	public MessageQueue() {
		kryo = new MqKryo();
		pair = new ByteArrayStreamPair();
		output = new Output(pair.getOutputStream());
		input = new Input(pair.getInputStream());
	}
	
	public void put(Message message) {
		kryo.writeObject(output, message);
	}
	
	public Message take() {
		return kryo.readObject(input, Message.class);
	}
	
	public boolean available() {
		return pair.available();
	}
}
