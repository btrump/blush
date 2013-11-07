package com.blairtrump.blush;

import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class NetworkCommunicator {
	private NetworkStatus status = NetworkStatus.IDLE;
	private int port = 5672;
	private String host = "localhost";
	protected String queue_name = "lobby";
	protected Connection connection = null;
	protected Channel channel = null;
	protected ConnectionFactory factory = new ConnectionFactory();
	protected QueueingConsumer consumer;
	private boolean connected;

	public NetworkCommunicator() {
		setStatus(NetworkStatus.UNINITIALIZED);
	}

	public boolean isConnected() {
		return connected;
	}

	public void setStatus(NetworkStatus status) {
		this.status = status;
	}

	public NetworkStatus getStatus() {
		return this.status;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
		this.factory.setPort(port);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
		this.factory.setHost(host);
	}

	public String getQueue_name() {
		return queue_name;
	}

	public void setQueue_name(String queue_name) {
		this.queue_name = queue_name;
	}

	public void initialize() throws Exception {
		setStatus(NetworkStatus.INITALIZING);
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		this.initialize(host, port, queue_name);
		setStatus(NetworkStatus.IDLE);
	}

	public void initialize(String host, int port, String lobby) {
		setStatus(NetworkStatus.INITALIZING);
		this.connection = null;
		this.channel = null;
		this.factory = new ConnectionFactory();
		this.setHost(host);
		this.setPort(port);
		this.queue_name = "lobby";
		setStatus(NetworkStatus.IDLE);
	}

	public void disconnect() {
		this.connected = false;
	}

	public void reportStatus() {
		String message;
		long timestamp = (new Date()).getTime();
		switch (getStatus()) {
		case LISTENING:
			message = String
					.format("[%s] Listening for messages on queue '%s' on server at %s:%s...",
							timestamp, queue_name, host, port);
			break;
		default:
			message = "What?";
		}
		System.out.println(message);
	}

	protected String handleMessage(QueueingConsumer.Delivery delivery) throws Exception {
		String payload = new String(delivery.getBody());
		Packet packet = Packet.fromJson(payload);
		String response = "";
		if (packet.isValid()) {
			try {
				System.out.format("Handle message: Got packet: %s", packet);
			} catch (Exception e) {
				System.err.format("Handle message: %s", e);
			}
		}
		response = packet.toJson();
	
		return response;
	}

}