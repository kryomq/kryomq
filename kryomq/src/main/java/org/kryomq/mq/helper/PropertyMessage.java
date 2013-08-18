package org.kryomq.mq.helper;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;
import org.kryomq.kryo.serializers.ChainWrapSerializer;
import org.kryomq.kryo.serializers.ChainWrapSerializer.Chained;
import org.kryomq.mq.BufferMessage;

public class PropertyMessage {
	protected transient Map<String, Object> props = new HashMap<String, Object>();

	protected byte[] buf;
	
	public Object get(String propertyName) {
		return props.get(propertyName);
	}
	
	public boolean has(String propertyName) {
		return props.containsKey(propertyName);
	}
	
	public Object put(String propertyName, Object value) {
		return props.put(propertyName, value);
	}
	
	public void store(Kryo kryo) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Output output = new Output(buf);
		kryo.writeClassAndObject(output, props);
		output.close();
		this.buf = buf.toByteArray();
	}
	
	public void load(Kryo kryo) {
		props = (Map<String, Object>) kryo.readClassAndObject(new Input(buf));
	}
	
	@Chained
	private void prewrite(Kryo kryo) {
		store(kryo);
	}
	
	@Chained
	private void postread(Kryo kryo) {
		load(kryo);
	}
}
