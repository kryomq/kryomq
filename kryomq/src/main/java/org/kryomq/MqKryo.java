package org.kryomq;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.FieldSerializer;

public class MqKryo extends Kryo {
	public MqKryo() {
		setReferences(false);
		setAutoReset(true);
		setRegistrationRequired(true);
		register(byte[].class);
		register(Message.class, new FieldSerializer<Message>(this, Message.class));
		register(Control.class, new FieldSerializer<Control>(this, Control.class));
		register(Control.Command.class);
		register(Meta.class, new FieldSerializer<Meta>(this, Meta.class));
		register(Meta.MetaType.class);
	}
}
