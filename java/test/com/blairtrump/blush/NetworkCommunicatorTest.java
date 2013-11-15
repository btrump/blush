package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class NetworkCommunicatorTest {
	protected final static String testString = "This is the test message string";

	public NetworkCommunicator getUnavailableMessageBroker() throws Exception {
		NetworkCommunicator communicator = new NetworkCommunicator();
		String host = "fake.example.com";
		int port = -1;
		String queue_name = testString;
		Long id = new Long(0);
		communicator.initialize(host, port, queue_name, id);
		return communicator;
	}
	
	@Test
	public void testInitializeAndGettersAndSetters() throws Exception {
		NetworkCommunicator server = new Server();
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		Long id = new Long(0);
		server.initialize(host, port, queue_name, id);

		boolean allMembersEqual = false;
		if (server.getHost().equals(host) && server.getPort() == port
				&& server.getQueue().equals(queue_name)) {
			allMembersEqual = true;
		}
		assertTrue(allMembersEqual);
	}
}
