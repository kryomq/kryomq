package org.kryomq.mqex.chat;

import java.util.EventObject;

public class ChatEvent extends EventObject {
	public static enum ChatEventType {
		MESSAGE_RECEIVED,
		USER_ONLINE,
		USER_OFFLINE,
		USER_AWAY,
		USER_RETURNED,
		USER_CHANGED_NICKNAME,
	}
	
	private ChatEventType type;
	private ChatMessage message;
	private StatusReport status;
	
	public ChatEvent(ChatClient source, StatusReport status) {
		super(source);
		this.type = status.getType().chatEventType();
		this.status = status;
	}
	
	public ChatEvent(ChatClient source, ChatEventType type, ChatMessage message) {
		super(source);
		this.type = type;
		this.message = message;
	}
	
	@Override
	public ChatClient getSource() {
		return (ChatClient) super.getSource();
	}
	
	public ChatEventType getType() {
		return type;
	}
	
	public ChatMessage getMessage() {
		return message;
	}
	
	public StatusReport getStatus() {
		return status;
	}
}
