package com.blairtrump.blush;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class Client extends NetworkCommunicator {
	public Client() throws Exception {
		super();
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
