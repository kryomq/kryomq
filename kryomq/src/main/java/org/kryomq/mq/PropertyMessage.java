package org.kryomq.mq;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.kryomq.kryo.Kryo;

public class PropertyMessage extends Message {
	private transient Map<String, Object> props = new HashMap<String, Object>();
	
	public PropertyMessage() {
	}

	public PropertyMessage(String topic, boolean reliable) {
		super(topic, reliable);
	}

	public PropertyMessage setProperty(String property, Object value) {
		props.put(property, value);
		return this;
	}
	
	@Override
	public PropertyMessage createReply() {
		return new PropertyMessage(origin, reliable);
	}
	
	public boolean hasProperty(String property) {
		return props.containsKey(property);
	}
	
	public Object getProperty(String property) {
		return props.get(property);
	}
	
	public void store(Kryo kryo) {
		set(kryo, props);
	}
	
	private void load(Kryo kryo) {
		props = (Map<String, Object>) get(kryo);
	}
}
