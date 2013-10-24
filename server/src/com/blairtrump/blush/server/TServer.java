package com.blairtrump.blush.server;

/* Gson for JSON en/decoding */
//import com.google.gson.Gson;

/* RabbitMQ libraries */
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


/* For timestamps */
import java.util.Date;

public class TServer {
	private final static int PORT = 5672;
	private final static String HOST = "localhost";
	private final static String QUEUE_NAME = "lobby";

	public static void main(String[] args) throws Exception {
		Connection connection = null;
		Channel channel = null;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(HOST);
			factory.setPort(PORT);

			// TODO: try/catch NoRouteToHostException
			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			channel.queueDeclare(QUEUE_NAME+"_chat", false, false, false, null);

			channel.basicQos(1);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(QUEUE_NAME, false, consumer);

			System.out
					.printf("[%s] Server: Listening for messages on queue '%s' on server at %s:%s...\n",
							(new Date()).getTime(), QUEUE_NAME, HOST, PORT);

			while (true) {
				String response = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();
				String message = "";
				try {
					message = new String(delivery.getBody(), "UTF-8");
					response = handleMessage(message);
				} catch (Exception e) {
					System.out.println("Error: " + e.toString());
					response = "";

				} finally {
					channel.basicPublish("", props.getReplyTo(), replyProps,
							response.getBytes("UTF-8"));
					channel.basicPublish("", QUEUE_NAME+"_chat", null, message.getBytes());
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(),
							false);
				}
				// String message = new String(delivery.getBody());
				// Worker worker = new Worker(message);
				// Thread thread = new Thread(worker);
				// thread.start();
				// // System.out.printf("[%s] Server: Message received: '%s'",
				// (new Date()).getTime(), message);
				// // System.out
				// //
				// .printf("[%s] Server: Message received.  Spawning worker in new thread.\n",
				// // (new Date()).getTime(), worker, thread);
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
	//
	// class Worker implements Runnable {
	// private String message;
	//
	// Worker(String message) {
	// this.message = message;
	// }
	//
	// public void run() {
	// System.out.printf("[%s] Worker '%s': received %s-byte message:\n",
	// (new Date()).getTime(), this, message.length());
	// System.out.println(message);
	// String response = null;
	// QueueingConsumer.Delivery delivery = consumer.n
	// // Packet packet = new Gson().fromJson(message, Packet.class);
	// // System.out.println(packet);
	// }

	private static String handleMessage(String message) {
		String response = "";
		if(message.equals("connect")) {
			System.out.printf("Connect string received.  Transferring client from lobby to Worker\n");
			response = "Connecting you to Worker";
		} else {
			System.out.printf("Got: '%s'\n", message);
			response = String.format("You sent '%s'", message);
		}
		
		return response;
	}
}