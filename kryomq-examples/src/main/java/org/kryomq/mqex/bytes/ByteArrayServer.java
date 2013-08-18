package org.kryomq.mqex.bytes;

import java.io.IOException;

import org.kryomq.mq.MqServer;

public class ByteArrayServer {

	private MqServer server;
	
	public ByteArrayServer(int port) throws IOException {
		server = new MqServer(port);
		server.start();
	}

	public void close() throws IOException {
		server.stop();
	}
}
