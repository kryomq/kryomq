package org.kryomq.mqex.bytes;

import java.io.IOException;

import org.kryomq.mq.Receiver;
import org.kryomq.mq.Sender;
import org.kryomq.mq.MqClient;

public class ByteArrayClient {
	private MqClient client;

	public ByteArrayClient(String host, int port) throws IOException {
		client = new MqClient(host, port);
		client.start();
	}
	
	public void close() throws IOException {
		client.stop();
	}
	
	public Sender<byte[]> sender(String topic) {
		return client.createSender(topic, true);
	}
	
	public Receiver<byte[]> receiver(String topic) {
		return client.createReceiver(topic);
	}

}
