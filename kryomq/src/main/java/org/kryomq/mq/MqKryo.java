package org.kryomq.mq;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.FieldSerializer;

/**
 * {@link Kryo} preconfigured for use with KryoMQ message transport
 * @author robin
 *
 */
public class MqKryo extends Kryo {
	public MqKryo() {
		setReferences(false);
		setAutoReset(true);
		setRegistrationRequired(true);
		
		register(byte[].class);
		register(Message.class);

		register(Control.class);
		register(Control.Command.class);
		
		register(Meta.class);
		register(Meta.MetaType.class);
		
		register(Permission.class);
		register(Permission.PermissionType.class);
	}
}
