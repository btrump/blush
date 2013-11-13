/*
 * Packet types
 * System -- for client/server system communication, such as:
 * 		- CONNECT, DISCONNECT, STATUS, TALK, PING, PASSTHROUGH
 * Application -- Pass-through of JSON message to application using Blush for messaging
 */
package com.blairtrump.blush;

import com.google.gson.Gson;

public class Packet {
	public enum Type {
		SYSTEM, APPLICATION, INVALID
	}

	public enum Command {
		CONNECT, DISCONNECT, STATUS, TALK, PING, PASSTHROUGH
	}

	private Type type;
	private String message;
	private Command command;
	private boolean valid = false;

	public Packet() {
	}

	public Packet(Packet.Type type, String message, Command command) {
		this.type = type;
		this.message = message;
		this.command = command;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public static Packet fromJson(String json) {
		Gson gson = new Gson();
		Packet packet = new Packet();
		try {
			packet = gson.fromJson(json, Packet.class);
			packet.setValidity(true);
		} catch (Exception e) {
			System.err.println("Packet::fromJson(): " + e);
		}
		return packet;
	}

	public String getMessage() {
		return message;
	}

	public Packet.Type getType() {
		return type;
	}
	
	public Packet.Command getCommand() {
		return command;
	}

	public boolean isValid() {
		return this.valid;
	}

	public void setValidity(boolean validity) {
		this.valid = validity;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean isSystem() {
		return getType().equals(Type.SYSTEM);
	}

	public String toString() {
		String validity = isValid() ? "V" : "NV";
		return String.format("%s %s (%s): %s", type, command, validity, message);
	}
}
