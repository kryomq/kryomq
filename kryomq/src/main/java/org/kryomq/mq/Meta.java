package org.kryomq.mq;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

/**
 * A statement about the KryoMQ system made by an {@link MqServer} to an {@link MqClient}
 * @author robin
 *
 */
public class Meta implements KryoSerializable {
	/**
	 * Types of KryoMQ metadata
	 * @author robin
	 *
	 */
	public static enum MetaType {
		/**
		 * Assign a unique numeric {@link MqClient} id
		 */
		ID,
		/**
		 * Assign a unique privileged topic.  See {@link Topics#PRIVILEGED}.
		 */
		PRIVILEGED_TOPIC,
		/**
		 * Assign a unique controlled topic. See {@link Topics#CONTROLLED}
		 */
		CONTROLLED_TOPIC,
		/**
		 * Some other {@link MqClient} has connected
		 */
		CONNECTED,
		/**
		 * Some other {@link MqClient} has disconnected
		 */
		DISCONNECTED,
	}
	
	/**
	 * The metadata type
	 */
	private MetaType type;
	/**
	 * The metadata topic, either beign assigned or the personal topic of the {@link MqClient} in question
	 */
	private String topic;
	
	/**
	 * required for deserialization
	 */
	@Deprecated
	public Meta() {}
	
	/**
	 * Create a new metadata message
	 * @param type
	 * @param topic
	 */
	public Meta(MetaType type, String topic) {
		this.type = type;
		this.topic = topic;
	}

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, type);
		output.writeString(topic);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		type = kryo.readObject(input, MetaType.class);
		topic = input.readString();
	}
	
	public MetaType type() {
		return type;
	}
	
	public String topic() {
		return topic;
	}
}
