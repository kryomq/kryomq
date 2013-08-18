package org.kryomq.mq;

import java.util.Arrays;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.KryoSerializable;
import org.kryomq.kryo.io.Input;
import org.kryomq.kryo.io.Output;

/**
 * Permissions grantable for potentially privileged actions
 * @author robin
 *
 */
public class Permission implements KryoSerializable {
	/**
	 * Types of permissions which can be granted
	 * @author robin
	 *
	 */
	public static enum PermissionType {
		/**
		 * Permission to set your {@link MqClient} origin
		 */
		SET_ORIGIN,
		/**
		 * Permission to subscribe to a topic
		 */
		SUBSCRIBE,
		/**
		 * Permission to control queueing of a topic
		 */
		QUEUE,
		/**
		 * Permission to send to a topic
		 */
		SEND,
		/**
		 * Permission to grant permission
		 */
		GRANT,
		/**
		 * Permission to revoke permission
		 */
		REVOKE,
	}
	
	/**
	 * The type of permission that is represented by this {@link Permission}
	 */
	private PermissionType type;
	/**
	 * The topic to which this {@link Permission} applies
	 */
	private String topic;
	
	/**
	 * required for deserialization
	 */
	@Deprecated
	public Permission() {}
	
	/**
	 * Create a {@link Permission} for a non-topical {@link PermissionType}
	 * @param type
	 */
	public Permission(PermissionType type) {
		this(type, null);
	}
	
	/**
	 * Create a {@link Permission} for a topical {@link PermissionType}
	 * @param type
	 * @param topic
	 */
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

	@Override
	public void write(Kryo kryo, Output output) {
		kryo.writeObject(output, type);
		output.writeString(topic);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		type = kryo.readObject(input, PermissionType.class);
		topic = input.readString();
	}
	
	public PermissionType type() {
		return type;
	}
	
	public String topic() {
		return topic;
	}
}
