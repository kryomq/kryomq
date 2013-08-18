package org.kryomq.mq;

/**
 * A statement about the KryoMQ system made by an {@link MqServer} to an {@link MqClient}
 * @author robin
 *
 */
public class Meta {
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
	public MetaType type;
	/**
	 * The metadata topic, either beign assigned or the personal topic of the {@link MqClient} in question
	 */
	public String topic;
	
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
}
