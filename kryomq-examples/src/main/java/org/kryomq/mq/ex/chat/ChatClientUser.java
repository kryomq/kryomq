package org.kryomq.mq.ex.chat;

public interface ChatClientUser extends ChatUser {
	public void setNickname(String nickname);
	public void setAway(boolean away);
	public void addChatListener(ChatListener l);
	public void removeChatListener(ChatListener l);
}
