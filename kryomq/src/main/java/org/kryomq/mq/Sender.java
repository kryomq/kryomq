package org.kryomq.mq;

public interface Sender<T> {
	public void send(T object);
	public void close();
}
