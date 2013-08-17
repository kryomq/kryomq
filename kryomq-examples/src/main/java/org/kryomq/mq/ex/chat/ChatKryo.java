package org.kryomq.mq.ex.chat;

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
		
		Serializer<ChatClient> ccs = new FieldSerializer<ChatClient>(this, ChatClient.class);
		register(ChatClient.class, new ChainWrapSerializer<ChatClient>(ChatClient.class, ccs));
		
		register(StatusReport.class);
		register(StatusReport.StatusType.class);
		
		getContext().put(OWNER_CONTEXT_KEY, owner);
	}
}
