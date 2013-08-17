package org.kryomq.mq;

import java.util.Arrays;

public class Permission {
	public static enum PermissionType {
		SET_ORIGIN("origin"),
		SUBSCRIBE("subscribe"),
		QUEUE("queue"),
		SEND("send"),
		GRANT("grant"),
		REVOKE("revoke"),
		;
		
		private String topic;
		
		private PermissionType(String topic) {
			this.topic = topic;
		}
		
		public String getTopic() {
			return topic;
		}
	}
	
	public PermissionType type;
	public String topic;
	
	public Permission(PermissionType type) {
		this(type, null);
	}
	
	public Permission(PermissionType type, String topic) {
		this.type = type;
		this.topic = topic;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] {type, topic});
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(obj instanceof Permission) {
			Permission p = (Permission) obj;
			if(type != p.type)
				return false;
			if(topic == null ? p.topic != null : !topic.equals(p.topic))
				return false;
			return true;
		}
		return false;
	}
}
