package org.kryomq.mq;

import java.util.HashMap;
import java.util.Map;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.ChainWrapSerializer;
import org.kryomq.kryo.serializers.ChainWrapSerializer.Chained;

/**
 * {@link BufferMessage} that keeps a transient {@link Map} of properties, independent
 * from the message buffer.  Call {@link #store(Kryo)} to write the properties map to
 * the message buffer.   Call {@link #load(Kryo)} to read the properties map from the
 * message buffer.
 * 
 * @author robin
 *
 */
public class PropertyMessage extends BufferMessage {
	/**
	 * The properties for this {@link PropertyMessage}
	 */
	protected transient Map<String, Object> props = new HashMap<String, Object>();
	
	/**
	 * Create an empty {@link PropertyMessage}
	 */
	public PropertyMessage() {
	}

	/**
	 * Create a {@link PropertyMessage} with specified destination and reliability
	 * @param topic
	 * @param reliable
	 */
	public PropertyMessage(String topic, boolean reliable) {
		super(topic, reliable);
	}

	/**
	 * Create a {@link PropertyMessage} with specified destination, buffer, and reliability
	 * @param topic
	 * @param buf
	 * @param reliable
	 */
	public PropertyMessage(String topic, byte[] buf, boolean reliable) {
		super(topic, buf, reliable);
	}

	@Override
	public PropertyMessage createReply() {
		return new PropertyMessage(origin, reliable);
	}
	
	@Override
	public PropertyMessage set(Kryo kryo, Object value) {
		return (PropertyMessage) super.set(kryo, value);
	}
	
	/**
	 * Set a property of this message
	 * @param property
	 * @param value
	 * @return
	 */
	public PropertyMessage setProperty(String property, Object value) {
		props.put(property, value);
		return this;
	}
	
	/**
	 * Return whether this message has a property
	 * @param property
	 * @return
	 */
	public boolean hasProperty(String property) {
		return props.containsKey(property);
	}
	
	/**
	 * Return a property of this message
	 * @param property
	 * @return
	 */
	public Object getProperty(String property) {
		return props.get(property);
	}
	
	/**
	 * Write the propreties to the message buffer
	 * @param kryo
	 */
	public void store(Kryo kryo) {
		set(kryo, props);
	}
	
	/**
	 * Load the properties from the message buffer
	 * @param kryo
	 */
	public void load(Kryo kryo) {
		props = (Map<String, Object>) get(kryo);
	}
	
	/**
	 * If we happen to be being serialized by a {@link ChainWrapSerializer}, then take advantage
	 * of that to ensure properties are stored prior to write
	 * @param kryo
	 */
	@Chained
	private void prewrite(Kryo kryo) {
		store(kryo);
	}
	
	/**
	 * If we happen to be being deserialized by a {@link ChainWrapSerializer}, then take advantage
	 * of that to ensure properties are loaded after read
	 * @param kryo
	 */
	@Chained
	private void postread(Kryo kryo) {
		load(kryo);
	}

}
