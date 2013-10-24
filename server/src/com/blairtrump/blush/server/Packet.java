package com.blairtrump.blush.server;

/* Gson for JSON en/decoding */
import com.google.gson.Gson;

public class Packet {
	private enum Type {
		CONNECT, DISCONNECT
	}

	private Type type;
	private int id;
	private String rand;
	private String payload;
	private String message;
	private boolean valid = false;

	public Packet(String payload) {
		Gson gson = new Gson();
		try {
			Packet packet = gson.fromJson(payload, Packet.class);
			this.type = Type.values()[packet.id];
			this.id = packet.id;
			this.rand = packet.rand;
			this.message = packet.message;
			this.valid = true;
		} catch (Exception e) {
			System.err.format("Invalid packet.  %s\n", e);
			this.valid = false;
		} finally {
			this.payload = payload;
		}
	}

	public boolean isValid() {
		return this.valid;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRand() {
		return rand;
	}

	public void setRand(String rand) {
		this.rand = rand;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
