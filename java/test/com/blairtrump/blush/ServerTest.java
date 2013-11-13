package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest extends NetworkCommunicatorTest {
	@Test
	public void testNoMessageBrokerAvailable() throws Exception {
		Server server = new Server(getUnavailableMessageBroker());
		assertFalse(server.connect());
	}
}
