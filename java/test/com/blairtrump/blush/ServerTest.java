package com.blairtrump.blush;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServerTest extends NetworkCommunicatorTest {
	@Test
	/*
	 * TODO: Make copy constructor for Server and Client classes that takes superclass NetworkCommunicator
	 */
	public void testNoMessageBrokerAvailable() throws Exception {
		Server server = (Server)getUnavailableMessageBroker();
		assertFalse(server.connect());
	}

//	Can't poll a listening server unless listener is on a separate thread
//	@Test
	public void testListen() throws Exception {
		Server server = new Server();
		server.initialize();
		server.connect();
		server.listen();
		assertEquals(server.getStatus(), NetworkStatus.LISTENING);
	}
}
