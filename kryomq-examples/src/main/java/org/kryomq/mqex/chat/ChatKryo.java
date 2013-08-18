package org.kryomq.mqex.chat;

import org.kryomq.kryo.Kryo;
import org.kryomq.kryo.Serializer;
import org.kryomq.kryo.serializers.ChainWrapSerializer;
import org.kryomq.kryo.serializers.FieldSerializer;

public class ChatKryo extends Kryo {
	public static final String OWNER_CONTEXT_KEY = ChatClient.class.getName() + ".owner";
	
	public ChatKryo(ChatClient owner) {
		setReferences(true);
		setRegistrationRequired(true);
		setAutoReset(true);
		
		setDefaultSerializer(FieldSerializer.class);
		
		register(StatusReport.class);
		register(StatusReport.StatusType.class);
		
		chained(ChatClient.class);
		chained(ChatMessage.class);
		
		getContext().put(OWNER_CONTEXT_KEY, owner);
	}
	
	protected <T> void chained(Class<T> cls) {
		register(cls, new ChainWrapSerializer<T>(cls, new FieldSerializer<T>(this, cls)));
	}
}
