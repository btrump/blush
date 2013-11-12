package com.blairtrump.blush;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer;

import java.util.Date;

public class Server extends NetworkCommunicator {
	public Server() {
		super();
	}

	public void listen() {
		try {
			while (true) {
				setStatus(NetworkStatus.LISTENING);
				reportStatus();
				String response = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();
				long timestamp = (new Date()).getTime();
				response = handleMessage(delivery);
				System.out
						.format("[%s] %s::listen() - Got message: %s\n", timestamp, this.getClass(), response);
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
