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
	protected String reply_queue_name;

	private boolean connected;

	public NetworkCommunicator() {
		setStatus(NetworkStatus.UNINITIALIZED);
	}

	public void log(String message) {
		String callingMethod = Thread.currentThread().getStackTrace()[2]
				.getMethodName() + "()";
		String[] canonicalClassName = Thread.currentThread().getStackTrace()[2]
				.getClassName().split("\\.");
		String className = canonicalClassName[canonicalClassName.length - 1];
		System.out.format("[%s] %s::%s - %s\n", new Date().getTime(),
				className, callingMethod, message);
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

	public String reportStatus() {
		String message;
		switch (getStatus()) {
		case LISTENING:
			message = String.format(
					"Listening for messages on queue '%s' on server at %s:%s",
					queue_name, host, port);
			break;
		default:
			message = "What?";
		}
		return message;
	}

	public boolean connect() throws Exception {
		boolean success = false;
		setStatus(NetworkStatus.CONNECTING);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			if (this.getClass().equals(Server.class)) {
				channel.queueDeclare(queue_name, false, false, false, null);
				channel.basicQos(1);
				consumer = new QueueingConsumer(channel);
				channel.basicConsume(queue_name, false, consumer);
			} else {
				reply_queue_name = channel.queueDeclare().getQueue();
				consumer = new QueueingConsumer(channel);
				channel.basicConsume(reply_queue_name, true, consumer);
			}
			success = true;
		} catch (java.io.IOException e) {
			System.err.format(
					"%s::connect(): Could not reach RabbitMQ broker - %s",
					this.getClass(), e);
		} finally {
			setStatus(NetworkStatus.IDLE);
		}
		return success;
	}

	protected String handleMessage(QueueingConsumer.Delivery delivery) {
		String payload = new String(delivery.getBody());
		Packet packet = Packet.fromJson(payload);
		String response = "";
		String message = String.format("Got packet: %s", packet.toString());
		log(message);
		if (packet.isValid()) {
			try {
				// if system command, call appropriate function
				if (packet.isSystem()) {
					switch (packet.getCommand()) {
					case CONNECT:
						break;
					case DISCONNECT:
						break;
					case PASSTHROUGH:
						break;
					case PING:
						break;
					case STATUS:
						response = reportStatus();
						break;
					case TALK:
						break;
					default:
						break;
					}
				}
				// if application, automatically pass message through
			} catch (Exception e) {
				System.err.format("[%s] %s::handleMessage() - %s", new Date()
						.getTime(), this.getClass().getCanonicalName(), e);
			}
		}

		return response;
	}
}
