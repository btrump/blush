package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest {
	private final static String testString = "This is the test message string";

	@Test
	public void testNoMessageBrokerAvailable() throws Exception {
		NetworkCommunicator server = new Server();
		String host = "fake.example.com";
		int port = -1;
		String queue_name = testString;
		server.initialize(host, port, queue_name);
//		assertFalse(server.connect());
	}

	@Test
	public void testInitializeAndGettersAndSetters() throws Exception {
		NetworkCommunicator server = new Server();
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		server.initialize(host, port, queue_name);

		boolean allMembersEqual = false;
		if (server.getHost().equals(host) && server.getPort() == port
				&& server.getQueue_name().equals(queue_name)) {
			allMembersEqual = true;
		}
		assertTrue(allMembersEqual);
	}

//	Can't poll a listening server unless listener is on a separate thread
//	@Test
//	public void testListen() throws Exception {
//		Server server = new Server();
//		server.initialize();
//		server.connect();
//		server.listen();
//		assertEquals(server.getStatus(), Server.Status.LISTENING);
//	}
}
