package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClientTest extends NetworkCommunicatorTest {
	@Test
	/*
	 * TODO: Make copy constructor for Server and Client classes that takes superclass NetworkCommunicator
	 */
	public void testNoMessageBrokerAvailable() throws Exception {
		Client client = (Client)getUnavailableMessageBroker();
		System.out.println(client.getClass());
		assertFalse(client.connect());
	}
}
