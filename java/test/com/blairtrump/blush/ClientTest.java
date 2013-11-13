package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClientTest extends NetworkCommunicatorTest {
	@Test
	public void testNoMessageBrokerAvailable() throws Exception {
		Client client = new Client(getUnavailableMessageBroker());
		assertFalse(client.connect());
	}
}
