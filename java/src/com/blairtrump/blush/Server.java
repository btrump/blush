package com.blairtrump.blush;

/* RabbitMQ libraries */
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/* For timestamps */
import java.util.Date;

public class Server {
	public enum Status {
		IDLE, LISTENING, SHUTTING_DOWN, INITALIZING, CONNECTING, UNINITIALIZED
	}

	private Status status = Status.IDLE;
	private int port = 5672;
	private String host = "localhost";
	private String queue_name = "lobby";
	private Connection connection = null;
	private Channel channel = null;
	private ConnectionFactory factory = new ConnectionFactory();
	private QueueingConsumer consumer;
	private boolean connected;

	public boolean isConnected() {
		return connected;
	}

	public void Server() {
		setStatus(Status.UNINITIALIZED);
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
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
		setStatus(Status.INITALIZING);
		String host = "localhost";
		int port = 5672;
		String queue_name = "lobby";
		this.initialize(host, port, queue_name);
		setStatus(Status.IDLE);
	}

	public void initialize(String host, int port, String lobby) {
		setStatus(Status.INITALIZING);
		this.connection = null;
		this.channel = null;
		this.factory = new ConnectionFactory();
		this.setHost(host);
		this.setPort(port);
		this.queue_name = "lobby";
		setStatus(Status.IDLE);
	}

	public void disconnect() {
		this.connected = false;
	}

	public boolean connect() throws Exception {
		boolean success = false;
		setStatus(Status.CONNECTING);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(queue_name, false, false, false, null);
			channel.basicQos(1);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(queue_name, false, consumer);
			success = true;
		} catch (java.net.ConnectException e) {
			System.err.println("connect(): " + e);
			success = false;
		} catch (java.net.NoRouteToHostException e) {
			System.err.println("connect(): " + e);
			success = false;
		} catch (java.net.UnknownHostException e) {
			System.err.println("Server::connect(): " + e);
			success = false;
		} finally {
			setStatus(Status.IDLE);
		}
		return success;
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

	private String handleMessage(QueueingConsumer.Delivery delivery)
			throws Exception {
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

	public void listen() {
		try {
			while (true) {
				setStatus(Status.LISTENING);
				reportStatus();
				String response = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();
				try {
					response = handleMessage(delivery);
					System.out.format("[%s] Got message: \n", response);
				} catch (Exception e) {
					System.err.println("Error: " + e.toString());
					response = "";
				} finally {
					channel.basicPublish("", props.getReplyTo(), replyProps,
							response.getBytes("UTF-8"));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
							false);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					System.err
							.println("Error while closing connection (ignored): "
									+ e);
				}
			}
			setStatus(Status.IDLE);
		}
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.initialize();
		if (server.connect()) {
			server.listen();
		}
	}
}
