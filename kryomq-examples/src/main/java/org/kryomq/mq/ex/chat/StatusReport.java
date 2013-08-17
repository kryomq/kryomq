package org.kryomq.mq.ex.chat;

import org.kryomq.mq.ex.chat.ChatEvent.ChatEventType;

public class StatusReport {
	public static enum StatusType {
		ONLINE,
		OFFLINE,
		AWAY,
		RETURNED,
		SET_NICKNAME
		;
		
		public ChatEventType chatEventType() {
			switch(this) {
			case ONLINE: return ChatEventType.USER_ONLINE;
			case OFFLINE: return ChatEventType.USER_OFFLINE;
			case AWAY: return ChatEventType.USER_AWAY;
			case RETURNED: return ChatEventType.USER_RETURNED;
			case SET_NICKNAME: return ChatEventType.USER_CHANGED_NICKNAME;
			}
			return null;
		}
	}
	
	private StatusType type;
	private ChatUser client;
	
	public StatusReport() {}
	
	public StatusReport(StatusType type, ChatUser client) {
		this.type = type;
		this.client = client;
	}
	
	public StatusType getType() {
		return type;
	}
	
	public ChatUser getClient() {
		return client;
	}
}
