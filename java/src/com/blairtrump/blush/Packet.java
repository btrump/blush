package com.blairtrump.blush;

import com.google.gson.*;

public class Packet {
	public enum Type {
		SYSTEM, APPLICATION, INVALID
	}

	private String message;
	private String payload;
	private Type type;
	private boolean valid = false;

	public Packet() {
	}

	public Packet(Packet.Type type, String message) {
		this.type = type;
		this.message = message;
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
		packet.setPayload(json);
		return packet;
	}

	public String getMessage() {
		return message;
	}

	public Packet.Type getType() {
		return type;
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

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getPayload() {
		return payload;
	}

	public String toString() {
		return String.format("%s (%s): %s", this.type, this.isValid() ? "V"
				: "NV", this.message);
	}
}
