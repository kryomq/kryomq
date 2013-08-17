package org.kryomq.mq.ex.chat;

import java.util.EventListener;

public interface ChatListener extends EventListener {
	public void messageReceived(ChatEvent e);
	public void statusChanged(ChatEvent e);
}
