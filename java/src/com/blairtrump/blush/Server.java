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
	private final static int PORT = 5672;
	private final static String HOST = "localhost";
	private final static String QUEUE_NAME = "lobby";
	private Connection connection = null;
	private Channel channel = null;
	private QueueingConsumer consumer;

	public Server() throws Exception {
	}

	public void initialize() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(HOST);
		factory.setPort(PORT);

		// TODO: try/catch NoRouteToHostException, ConnectException
		connection = factory.newConnection();
		channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicQos(1);
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(QUEUE_NAME, false, consumer);
		System.out
				.format("[%s] Server: Listening for messages on queue '%s' on server at %s:%s...\n",
						(new Date()).getTime(), QUEUE_NAME, HOST, PORT);
	}

	private String handleMessage(QueueingConsumer.Delivery delivery)
			throws Exception {
		String payload = new String(delivery.getBody());
		Packet packet = Packet.fromJson(payload);
		String response = "";
		if (packet.isValid()) {
			try {
				System.out.format("Handle message: Got packet: %s\n", packet);
			} catch (Exception e) {
				System.err.format("Handle message: %s\n", e);
			}
		}
		response = packet.toJson();

		return response;
	}

	public void listen() {
		try {
			while (true) {
				String response = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();
				try {
					response = handleMessage(delivery);
					System.out.println(response);
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
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception ignore) {

				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.initialize();
		server.listen();
	}
}