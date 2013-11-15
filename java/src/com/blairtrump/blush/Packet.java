/*
 * Packet types
 * System -- for client/server system communication, such as:
 * 		- CONNECT, DISCONNECT, STATUS, TALK, PING, PASSTHROUGH
 * Application -- Pass-through of JSON message to application using Blush for messaging
 */
package com.blairtrump.blush;

import java.util.Date;

import com.google.gson.Gson;
import com.rabbitmq.client.QueueingConsumer;

public class Packet {
	public enum Command {
		CONNECT, DISCONNECT, LIST_INSTANCES, PASSTHROUGH, PING, STATUS, TALK
	}

	public enum Type {
		APPLICATION, INVALID, SYSTEM
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
	private Command command;
	private String message;
	private String replyQueue;
	private Long senderId;
	private Long timestamp;
	private Type type;

	private boolean valid = false;
	public Packet() {
	}

	public Packet(Packet.Type type, String message, Command command, Long senderId) {
		this.type = type;
		this.message = message;
		this.command = command;
		this.senderId = senderId;
		timestamp = new Date().getTime();
	}

	public Packet(QueueingConsumer.Delivery delivery) {
		String payload = new String(delivery.getBody());
		Packet packet = Packet.fromJson(payload);
		this.type = packet.type;
		this.message = packet.message;
		this.command = packet.command;
		this.senderId = packet.senderId;
		setTimestamp(delivery.getProperties().getTimestamp().getTime());
		this.replyQueue = delivery.getProperties().getReplyTo();
		this.setValidity(true);
	}

	public Packet.Command getCommand() {
		return command;
	}
	
	public String getMessage() {
		return message;
	}

	public String getReplyQueue() {
		return replyQueue;
	}

	public Long getSenderId() {
		return senderId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public Packet.Type getType() {
		return type;
	}

	public boolean isSystem() {
		return getType().equals(Type.SYSTEM);
	}

	public boolean isValid() {
		return this.valid;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setReplyQueue(String replyQueue) {
		this.replyQueue = replyQueue;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setValidity(boolean validity) {
		this.valid = validity;
	}
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String toString() {
		String validity = isValid() ? "V" : "NV";
		return String.format("%s %s (%s): %s", type, command, validity, message);
	}
}
