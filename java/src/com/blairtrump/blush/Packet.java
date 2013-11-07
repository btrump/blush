package com.blairtrump.blush;

import com.google.gson.*;

public class Packet {
	private String message;
	private String payload;
	private int type;
	private boolean valid = false;
	
	public Packet(){}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static Packet fromJson(String json) {
		Gson gson = new Gson();
		Packet packet = new Packet();
		try {
			packet = gson.fromJson(json, Packet.class);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.err.format("ArrayIndexOutOfBoundsException: %s", json);
		} 
		packet.setPayload(json);
		return packet;
	}

	public String getMessage() {
		return message;
	}

	public int getType() {
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

	public void setType(int type) {
		this.type = type;
	}
	
	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	public String getPayload() {
		return payload;
	}
	
	public String toString() {
		return String.format("%s (%s): %s", this.type, this.isValid() ? "V" : "NV", this.message);
	}
}
