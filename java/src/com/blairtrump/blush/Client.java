package com.blairtrump.blush;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class Client extends NetworkCommunicator {
	private String reply_queue_name;

	public Client() throws Exception {
		super();
	}

	public boolean connect() throws Exception {
		boolean success = false;
		setStatus(NetworkStatus.CONNECTING);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			reply_queue_name = channel.queueDeclare().getQueue();
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(reply_queue_name, true, consumer);
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
			setStatus(NetworkStatus.IDLE);
		}

		return success;
	}

	public String call(String message) throws Exception {
		Packet packet = new Packet();
		packet.setMessage(message);
		String response = null;
		String corrId = UUID.randomUUID().toString();

		BasicProperties props = new BasicProperties.Builder()
				.correlationId(corrId).replyTo(reply_queue_name).build();

		System.out.format("\t[>] Sending message '%s' as packet '%s'\n",
				message, packet.toJson());
		channel.basicPublish("", queue_name, props, packet.toJson().getBytes());

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response = new String(delivery.getBody(), "UTF-8");
				System.out.format("\t[<] Got '%s'\n", response);
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String prompt = String.format("[%s] blush.Client~# ", timestamp);

		System.out.print(prompt);
		return reader.readLine();
	}

	public static void main(String[] argv) {
		Client client = null;
		try {
			client = new Client();
			client.initialize();
			if (client.connect()) {
				String response;
				String message;
				while (true) {
					message = client.prompt();
					response = client.call(message);
				}
			} else {
				System.err.println("Could not connect");
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
