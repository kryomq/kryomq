package org.kryomq.mq.ex.chat;

import java.io.IOException;

public interface ChatUser {
	public String getNickname();
	public void send(String text);
}
