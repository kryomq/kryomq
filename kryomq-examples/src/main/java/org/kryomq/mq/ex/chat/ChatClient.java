package org.kryomq.mq.ex.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.EventListenerList;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.ChainWrapSerializer.Chained;
import org.kryomq.kryonet.Connection;
import org.kryomq.mq.Message;
import org.kryomq.mq.MessageListener;
import org.kryomq.mq.Meta;
import org.kryomq.mq.MqClient;
import org.kryomq.mq.ex.chat.ChatEvent.ChatEventType;
import org.kryomq.mq.ex.chat.StatusReport.StatusType;

public class ChatClient implements ChatUser, MessageListener {
	private transient MqClient mq;
	private transient ChatClient owner;
	
	private String nickname;
	private String personalTopic;
	
	private transient Map<String, ChatClient> users = new HashMap<String, ChatClient>();
	private transient EventListenerList listenerList;
	
	public ChatClient() {
	}
	
	protected ChatClient(ChatClient owner) {
		this.owner = owner;
	}
	
	@Chained
	private void postread(Kryo kryo) {
		Object contextOwner = kryo.getContext().get(ChatKryo.OWNER_CONTEXT_KEY);
		if(contextOwner instanceof ChatClient)
			this.owner = (ChatClient) contextOwner;
	}
	
	public void connect(String host, int port) throws IOException {
		mq = new ChatMqClient(host, port);
		mq.start();
		personalTopic = mq.getPersonalTopic();
		
		mq.subscribe(personalTopic, this);
		mq.subscribe(ChatTopics.STATUS_REPORTS, this);
		
		users.put(personalTopic, this);
	}
	
	public void disconnect() throws IOException {
		mq.stop();
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
		if(mq == null)
			return;
		StatusReport report = new StatusReport(StatusType.SET_NICKNAME, this);
		Message m = new Message(ChatTopics.STATUS_REPORTS, true).set(new ChatKryo(this), report);
		mq.send(m);
	}
	
	protected ChatClient withPersonalTopic(String personalTopic) {
		this.personalTopic = personalTopic;
		return this;
	}
	
	protected String getPersonalTopic() {
		return personalTopic;
	}
	
	public void addChatListener(ChatListener l) {
		listenerList.add(ChatListener.class, l);
	}
	
	public void removeChatListener(ChatListener l) {
		listenerList.remove(ChatListener.class, l);
	}
	
	protected void fireMessageReceived(ChatMessage m) {
		Object[] ll = listenerList.getListenerList();
		ChatEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == ChatListener.class) {
				if(e == null)
					e = new ChatEvent(m.getFromUser(), ChatEventType.MESSAGE_RECEIVED, m);
				((ChatListener) ll[i+1]).messageReceived(e);
			}
		}
	}
	
	protected void fireStatusChanged(StatusReport report) {
		Object[] ll = listenerList.getListenerList();
		ChatEvent e = null;
		for(int i = ll.length - 2; i >= 0; i -= 2) {
			if(ll[i] == ChatListener.class) {
				if(e == null)
					e = new ChatEvent(this, report);
				((ChatListener) ll[i+1]).statusChanged(e);
			}
		}
	}
	
	public void sendTo(ChatClient user, String text) {
		ChatMessage m = new ChatMessage(user.getPersonalTopic());
		m.setText(text);
		m.setTimestamp(System.currentTimeMillis());
		m.setFromUser(this);
		m.setToUser(user);
		mq.send(m);
	}
	
	public void send(String text) {
		if(owner != null)
			owner.sendTo(this, text);
		else if(mq != null)
			sendTo(this, text);
		else
			throw new IllegalStateException();
	}

	public void messageReceived(Message message) {
		if(message instanceof ChatMessage) {
			fireMessageReceived((ChatMessage) message);
		} else {
			Object content = message.get(new ChatKryo(this));
			if(content instanceof StatusReport) {
				StatusReport report = (StatusReport) content;
				if(report.getType() == StatusType.SET_NICKNAME) {
					users.get(report.getClient().getPersonalTopic()).setNickname(report.getClient().getNickname());
				}
				fireStatusChanged(report);
			}
		}
	}
	
	public void setAway(boolean away) {
		StatusReport report = new StatusReport(away ? StatusType.AWAY : StatusType.RETURNED, this);
		Message m = new Message(ChatTopics.STATUS_REPORTS, true);
		m.set(new ChatKryo(this), report);
		mq.send(m);
	}
	
	public Map<String, ChatClient> getUsers() {
		return users;
	}

	private class ChatMqClient extends MqClient {
		private ChatMqClient(String host, int port) {
			super(host, port);
		}
		
		@Override
		public void received(Connection connection, Object object) {
			super.received(connection, object);
			if(object instanceof Meta) {
				Meta meta = (Meta) object;
				ChatClient source = new ChatClient(ChatClient.this).withPersonalTopic(meta.topic);
				switch(meta.type) {
				case CONNECTED:
					users.put(meta.topic, source);
					fireStatusChanged(new StatusReport(StatusType.ONLINE, source));
					break;
				case DISCONNECTED:
					users.remove(meta.topic);
					fireStatusChanged(new StatusReport(StatusType.OFFLINE, source));
				}
			}
		}
	}
	
	@Override
	public int hashCode() {
		return getPersonalTopic().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(obj instanceof ChatClient) {
			return getPersonalTopic().equals(((ChatClient) obj).getPersonalTopic());
		}
		return false;
	}
}