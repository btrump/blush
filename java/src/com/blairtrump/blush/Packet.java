package com.blairtrump.blush;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class Packet {
	public enum Type {
		APPLICATION, SYSTEM;
	}

	private String message;
	private String payload;
	private int type;
	private boolean valid = false;

	public Packet(String payload) {
		Gson gson = new Gson();
		try {
			Packet packet = gson.fromJson(payload, Packet.class);
//			this.type = Packet.Type.values()[Integer.parseInt(packet.type.to)];
			this.type = packet.type;
			this.payload = payload;
			this.message = packet.message;
			this.valid = true;
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.err.format("ArrayIndexOutOfBoundsException: %s", payload);
		} finally {
			this.payload = payload;
		}
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

	public void setMessage(String message) {
		this.message = message;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String toString() {
		return String.format("%s (%s): %s", this.type, this.isValid() ? "V" : "NV", this.message);
	}
}
