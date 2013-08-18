package org.kryomq.mq.ex.chat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kryomq.mq.MqServer;
import org.kryomq.mq.ex.ExTest;
import org.kryomq.mqex.chat.ChatClient;
import org.kryomq.mqex.chat.ChatClientUser;

public class ChatClientTest extends ExTest {
	private MqServer server;
	
	@Before
	public void before() throws Exception {
		server = new MqServer(1024 + (int)(Math.random() * (65536 - 1024)));
		server.start();
	}
	
	@Test
	public void testConnect() throws Exception {
		ChatClientUser lhs = new ChatClient();
		lhs.setNickname("lhs");
		lhs.connect("localhost", server.getPort());
		
		ChatClientUser rhs = new ChatClient();
		rhs.setNickname("rhs");
		rhs.connect("localhost", server.getPort());
		
		Thread.sleep(1000);
		
		Assert.assertTrue(lhs.getUsers().size() == 2);
		Assert.assertTrue(rhs.getUsers().size() == 2);
	}
	
	
	@After
	public void after() throws Exception {
		server.stop();
	}
}
