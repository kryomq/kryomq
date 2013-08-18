package org.kryomq.mqex.chat;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.ChainWrapSerializer.Chained;
import org.kryomq.mq.PropertyMessage;

public class ChatMessage extends PropertyMessage {
	private static final String TIMESTAMP = "timestamp";
	private static final String TEXT = "text";
	private static final String FROM_USER = "from-user";
	private static final String TO_USER = "to-user";
	
	public ChatMessage() {
	}

	public ChatMessage(String topic) {
		super(topic, true);
	}
	
	@Chained
	private void prewrite(Kryo kryo) {
		store(kryo);
	}
	
	@Chained
	private void postread(Kryo kryo) {
		load(kryo);
	}
	
	@Override
	public PropertyMessage createReply() {
		return new ChatMessage(origin);
	}
	
	public Long getTimestamp() {
		return (Long) getProperty(TIMESTAMP);
	}
	public void setTimestamp(Long timestamp) {
		setProperty(TIMESTAMP, timestamp);
	}
	public String getText() {
		return (String) getProperty(TIMESTAMP);
	}
	public void setText(String text) {
		setProperty(TEXT, text);
	}
	public ChatClient getFromUser() {
		return (ChatClient) getProperty(FROM_USER);
	}
	public void setFromUser(ChatClient fromUser) {
		setProperty(FROM_USER, fromUser);
	}
	public ChatClient getToUser() {
		return (ChatClient) getProperty(TO_USER);
	}
	public void setToUser(ChatClient toUser) {
		setProperty(TO_USER, toUser);
	}
	
	public void reply(String text) {
		getToUser().sendTo(getFromUser(), text);
	}
}
