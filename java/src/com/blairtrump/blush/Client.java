package com.blairtrump.blush;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class Client {
	private Connection connection;
	private Channel channel;
	private String requestQueueName = "lobby";
	private String replyQueueName;
	private QueueingConsumer consumer;

	public Client() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel = connection.createChannel();

		replyQueueName = channel.queueDeclare().getQueue();
		consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);
	}
	
	public String call(String message) throws Exception {
		String response = null;
		String corrId = UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(replyQueueName).build();

		channel.basicPublish("", requestQueueName, props, message.getBytes());

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody(), "UTF-8");
				break;
			}
		}

		return response;
	}

	public void close() throws Exception {
		connection.close();
	}
	
	public String prompt() throws Exception {
		long timestamp = (new Date()).getTime();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String prompt = String.format("[%s] blush.Client~# ", timestamp);

		System.out.print(prompt);
		return reader.readLine();
	}

	public static void main(String[] argv) {
		Client client = null;
		String response = null;
		String message;
		try {
			client = new Client();
			while(true) {
				message = client.prompt();
				System.out.format("\t[>] Sending '%s'\n", message);
				response = client.call(message);
				System.out.format("\t[<] Got '%s'\n", response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
}
