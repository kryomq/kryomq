package org.kryomq.mq;

public interface MessageListener {
	public void messageReceived(Message message);
}
