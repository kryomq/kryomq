package org.kryomq.mq.ex.chat;

import java.io.IOException;
import java.util.Set;

public interface ChatClientUser extends ChatUser {
	public void connect(String host, int port) throws IOException;
	public void disconnect() throws IOException;
	public void setNickname(String nickname);
	public void setAway(boolean away);
	public void addChatListener(ChatListener l);
	public void removeChatListener(ChatListener l);
	public Set<ChatUser> getUsers();
}
