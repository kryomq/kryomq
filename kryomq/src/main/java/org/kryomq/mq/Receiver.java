package org.kryomq.mq;

public interface Receiver<T> {
	public T receive();
	public boolean available();
	public void close();
}
