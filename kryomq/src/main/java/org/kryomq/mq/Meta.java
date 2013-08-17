package org.kryomq.mq;

public class Meta {
	public static enum MetaType {
		ID,
		PERSONAL_TOPIC,
		CONTROLLED_TOPIC,
		CONNECTED,
		DISCONNECTED,
	}
	
	public MetaType type;
	public String topic;
	
	public Meta() {}
	
	public Meta(MetaType type, String topic) {
		this.type = type;
		this.topic = topic;
	}
}
