package org.kryomq;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kryomq.Message;
import org.kryomq.MessageListener;
import org.kryomq.MqClient;
import org.kryomq.MqServer;

public class ControlTest extends MqTest {
	private MqServer server;
	
	@Before
	public void before() throws Exception {
		server = new MqServer(11223);
		server.start();
	}
	
	@Test
	public void testSetQueue() throws Exception {
		MqClient client;
		
		client = new MqClient("localhost", server.getPort());
		client.start();
		
		client.setQueue("test");
		client.send(new Message("test", true));
		
		final CountDownLatch latch = new CountDownLatch(1);
		client.subscribe("test", new MessageListener() {
			@Override
			public void messageReceived(Message message) {
				latch.countDown();
			}
		});
		
		Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
	}
	
	@After
	public void after() throws Exception {
		server.stop();
	}
}
