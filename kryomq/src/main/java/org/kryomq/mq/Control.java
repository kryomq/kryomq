package org.kryomq.mq;

/**
 * An object representing an action taken by an {@link MqClient} that must be sent
 * to the {@link MqServer}.
 * @author robin
 *
 */
public class Control {
	/**
	 * The command types available to an {@link MqClient}
	 * @author robin
	 *
	 */
	public static enum Command {
		/**
		 * Subscribe to a topic
		 */
		SUBSCRIBE,
		/**
		 * Unsubscribe from a topic
		 */
		UNSUBSCRIBE,
		/**
		 * Set the local origin topic
		 */
		SET_ORIGIN,
		/**
		 * Turn on queueing for a topic
		 */
		SET_QUEUE,
		/**
		 * Turn off queueing for a topic
		 */
		UNSET_QUEUE,
	}
	
	/**
	 * The command type
	 */
	public Command command;
	/**
	 * The topic the command is applied to
	 */
	public String topic;
	
	/**
	 * required for deserialization
	 */
	@Deprecated
	public Control() {}
	
	/**
	 * Create a new {@link MqClient} control command
	 * @param command The command type
	 * @param topic The topic applied to
	 */
	public Control(Command command, String topic) {
		this.command = command;
		this.topic = topic;
	}
}
