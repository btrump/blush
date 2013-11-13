package com.blairtrump.blush;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer;

public class Server extends NetworkCommunicator {
	public Server() {
		super();
	}

	public Server(NetworkCommunicator nc) {
		port = nc.getPort();
		host = nc.getHost();
		status = nc.getStatus();
		queue_name = nc.getQueue_name();
		connection = nc.connection;
		channel = nc.channel;
		factory = nc.factory;
		consumer = nc.consumer;
		reply_queue_name = nc.reply_queue_name;
	}

	public void listen() {
		try {
			while (true) {
				setStatus(NetworkStatus.LISTENING);
				log(reportStatus());
				String response = null;
				String message = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();
				response = handleMessage(delivery);
				message = String.format("Returning message: '%s'",
						response);
				log(message);
				channel.basicPublish("", props.getReplyTo(), replyProps,
						response.getBytes("UTF-8"));
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
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
			setStatus(NetworkStatus.IDLE);
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
