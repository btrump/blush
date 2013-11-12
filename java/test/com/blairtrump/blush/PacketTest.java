package com.blairtrump.blush;

import static org.junit.Assert.*;
import com.google.gson.*;
import org.junit.Test;

public class PacketTest {
	private final static String testMessage = "This is the test message string";

	@Test
	public void testFromJson() {
		final String rand = "726ca2be-1c80-4c49-85ee-0e66ab160d5a";
		final Packet.Type type = Packet.Type.values()[0];
		final String testJson = "{\"rand\": \"" + rand + "\","
				+ "\"message\": \"" + PacketTest.testMessage + "\","
				+ "\"type\": \"" + type + "\"}";
		Gson gson = new Gson();
		Packet packet = gson.fromJson(testJson, Packet.class);
		boolean allMembersEqual = false;
		if(type == packet.getType() && PacketTest.testMessage.equals(packet.getMessage())) {
			allMembersEqual = true;
		}
		assertTrue(allMembersEqual);
	}

	@Test
	public void testIsValid() {
		Packet packet = new Packet();
		packet.setValidity(true);
		assertEquals(true, packet.isValid());
	}
	
	@Test
	public void testIsNotValid() {
		Packet packet = new Packet();
		packet.setValidity(false);
		assertEquals(false, packet.isValid());
	}

	@Test
	public  void testGetAndSetMessage() {
		Packet packet = new Packet();
		packet.setMessage(PacketTest.testMessage);
		assertEquals(PacketTest.testMessage, packet.getMessage());
	}

	@Test
	public void testGetAndSetType() {
		Packet packet = new Packet();
		for(Packet.Type type : Packet.Type.values()) {
			packet.setType(type);
			assertEquals(type, packet.getType());
		}
	}

	@Test
	public void testToString() {
		Packet packet = new Packet();
		packet.setMessage(PacketTest.testMessage);
		assertEquals(String.class, packet.getMessage().getClass());
	}
}
