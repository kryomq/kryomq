package org.kryomq.mq;

public class Control {
	public static enum Command {
		SUBSCRIBE,
		UNSUBSCRIBE,
		SET_ORIGIN,
		SET_QUEUE,
		UNSET_QUEUE,
	}
	
	public Command command;
	public String topic;
	
	public Control() {}
	
	public Control(Command command, String topic) {
		this.command = command;
		this.topic = topic;
	}
}
