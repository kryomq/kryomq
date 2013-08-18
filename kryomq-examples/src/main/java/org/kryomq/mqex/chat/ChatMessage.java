package org.kryomq.mqex.chat;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.ChainWrapSerializer.Chained;
import org.kryomq.mq.helper.PropertyMessage;

public class ChatMessage extends PropertyMessage {
	private static final String TIMESTAMP = "timestamp";
	private static final String TEXT = "text";
	private static final String FROM_USER = "from-user";
	private static final String TO_USER = "to-user";
	
	public ChatMessage() {
	}

	public Long getTimestamp() {
		return (Long) get(TIMESTAMP);
	}
	public void setTimestamp(Long timestamp) {
		put(TIMESTAMP, timestamp);
	}
	public String getText() {
		return (String) get(TIMESTAMP);
	}
	public void setText(String text) {
		put(TEXT, text);
	}
	public ChatClient getFromUser() {
		return (ChatClient) get(FROM_USER);
	}
	public void setFromUser(ChatClient fromUser) {
		put(FROM_USER, fromUser);
	}
	public ChatClient getToUser() {
		return (ChatClient) get(TO_USER);
	}
	public void setToUser(ChatClient toUser) {
		put(TO_USER, toUser);
	}
	
	public void reply(String text) {
		getFromUser().send(text);
	}
}
