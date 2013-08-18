package org.kryomq.mqex.bytes;

import java.io.IOException;

import org.kryomq.mq.ByteArrayReceiver;
import org.kryomq.mq.ByteArraySender;
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
	
	public ByteArraySender sender(String topic) {
		return client.createSender(topic, true);
	}
	
	public ByteArrayReceiver receiver(String topic) {
		return client.createReceiver(topic);
	}

}
