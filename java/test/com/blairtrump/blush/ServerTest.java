package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest {
	private final static String testString = "This is the test message string";

	@Test
	public void testNoMessageBrokerAvailable() throws Exception {
		Server server = new Server();
		server.initialize();
		server.listen();
	}
	
	@Test
	public void testInitializeAndGettersAndSetters() throws Exception {
		Server server = new Server();
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		server.initialize(host, port, queue_name);
		
		boolean allMembersEqual = false;
		if(server.getHost().equals(host) && server.getPort() == port && server.getQueue_name().equals(queue_name))
			allMembersEqual = true;
		assertTrue(allMembersEqual);
	}

	@Test
	public void testListen() {
		fail("Not yet implemented");
	}
}
