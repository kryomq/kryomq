package org.kryomq.mq.ex.chat;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.serializers.FieldSerializer;

public class ChatKryo extends Kryo {
	public ChatKryo() {
		setReferences(true);
		setRegistrationRequired(true);
		setAutoReset(true);
		
		setDefaultSerializer(FieldSerializer.class);
		
		register(ChatUser.class);
		register(StatusReport.class);
		register(StatusReport.StatusType.class);
	}
}
