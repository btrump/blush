package com.blairtrump.blush;

import java.util.Date;
import java.util.Random;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class NetworkCommunicator {
	protected Channel channel;
	private boolean connected;
	protected Connection connection;
	protected QueueingConsumer consumer;
	protected ConnectionFactory factory;
	protected String host;
	protected Long id;
	protected Integer port;
	protected String queue;
	protected String replyQueue;

	protected NetworkStatus status;

	public NetworkCommunicator() {
		setStatus(NetworkStatus.UNINITIALIZED);
	}

	public boolean connect() throws Exception {
		boolean success = false;
		setStatus(NetworkStatus.CONNECTING);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			String message = null;
			if (this.getClass().equals(Server.class)) {
				channel.queueDeclare(queue, false, false, false, null);
				channel.basicQos(1);
				consumer = new QueueingConsumer(channel);
				channel.basicConsume(queue, false, consumer);
				message = String.format(
						"Connected to rabbitmq@%s:%s, consuming queue '%s'",
						getHost(), getPort(), getQueue());
			} else {
				replyQueue = channel.queueDeclare().getQueue();
				consumer = new QueueingConsumer(channel);
				channel.basicConsume(replyQueue, true, consumer);
				message = String
						.format("Connected to rabbitmq@%s:%s, consuming queue '%s' and reply queue '%s'",
								getHost(), getPort(), getQueue(),
								getReplyQueue());
			}
			success = true;
			log(message);
		} catch (java.io.IOException e) {
			System.err.format(
					"%s::connect(): Could not reach RabbitMQ broker - %s",
					this.getClass(), e);
		} finally {
			setStatus(NetworkStatus.IDLE);
		}
		return success;
	}

	public void disconnect() {
		this.connected = false;
	}

	public String getHost() {
		return host;
	}

	public Long getId() {
		return id;
	}

	public int getPort() {
		return port;
	}

	public String getQueue() {
		return queue;
	}

	public String getReplyQueue() {
		return replyQueue;
	}

	public NetworkStatus getStatus() {
		return this.status;
	}

	public void initialize() throws Exception {
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		Long id = (long) new Random(new Date().getTime()).nextInt();
		this.initialize(host, port, queue_name, id);
	}

	public void initialize(String host, int port, String lobby, Long id) {
		setStatus(NetworkStatus.INITALIZING);
		this.connection = null;
		this.channel = null;
		this.factory = new ConnectionFactory();
		this.setHost(host);
		this.setPort(port);
		this.queue = "lobby";
		setId(id);
		setStatus(NetworkStatus.IDLE);
	}

	public boolean isConnected() {
		return connected;
	}

	public static void log(String message) {
		String callingMethod = Thread.currentThread().getStackTrace()[2]
				.getMethodName() + "()";
		String[] canonicalClassName = Thread.currentThread().getStackTrace()[2]
				.getClassName().split("\\.");
		String className = canonicalClassName[canonicalClassName.length - 1];
		System.out.format("[%s] %s::%s - %s\n", new Date().getTime(),
				className, callingMethod, message);
	}
	
	public static void errorlog(String errorMessage, Exception e) {
		String callingMethod = Thread.currentThread().getStackTrace()[2]
				.getMethodName() + "()";
		String[] canonicalClassName = Thread.currentThread().getStackTrace()[2]
				.getClassName().split("\\.");
		String className = canonicalClassName[canonicalClassName.length - 1];
		System.err.format("[%s] %s::%s - %s\n", new Date().getTime(),
				className, callingMethod, errorMessage);
		e.printStackTrace(System.err);
	}

	public String reportStatus() {
		String message;
		switch (getStatus()) {
		case LISTENING:
			message = String.format(
					"Listening for messages on queue '%s' on server at %s:%s",
					queue, host, port);
			break;
		default:
			message = "What?";
		}
		return message;
	}

	public void setHost(String host) {
		this.host = host;
		this.factory.setHost(host);
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPort(int port) {
		this.port = port;
		this.factory.setPort(port);
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setStatus(NetworkStatus status) {
		this.status = status;
	}
}
