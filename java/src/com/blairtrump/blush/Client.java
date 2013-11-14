package com.blairtrump.blush;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.Date;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client extends NetworkCommunicator {
	public Client() throws Exception {
		super();
	}

	public Client(NetworkCommunicator nc) {
		port = nc.getPort();
		host = nc.getHost();
		status = nc.getStatus();
		queue = nc.getQueue();
		connection = nc.connection;
		channel = nc.channel;
		factory = nc.factory;
		consumer = nc.consumer;
		replyQueue = nc.replyQueue;
	}

	public Packet generator(int i) {
		System.out.println(i);
		String message = null;
		Packet.Type type = null;
		Packet.Command command = null;
		boolean valid;

		switch (i) {
		case 0:
			// Test system status report
			message = "I am requesting a status report";
			type = Packet.Type.SYSTEM;
			command = Packet.Command.STATUS;
			valid = true;
			break;
		case 1:
			// Test connect to new instance
			message = "Requesting connection to new instance";
			type = Packet.Type.SYSTEM;
			command = Packet.Command.CONNECT;
			valid = true;
			break;
		case 2:
			message = "This is a test application message";
			type = Packet.Type.APPLICATION;
			valid = true;
			break;
		default:
			message = "You passed an invalid argument to the generator";
			type = Packet.Type.INVALID;
			valid = false;
			break;
		}

		Packet packet = new Packet(type, message, command, this.getId());
		packet.setValidity(valid);
		return packet;
	}

	public String call(String message) throws Exception {
		Packet packet = generator(Integer.parseInt(message));
		String response = null;
		String corrId = UUID.randomUUID().toString();
		Date timestamp = new Date();
		BasicProperties props = new BasicProperties.Builder()
				.timestamp(timestamp).correlationId(corrId).replyTo(replyQueue)
				.build();
		System.out.format("\t[>] Sending message '%s' as packet '%s'\n",
				message, packet.toJson());
		channel.basicPublish("", queue, props, packet.toJson().getBytes());
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
		log("Closing connection");
		connection.close();
		log("Connection closed");
	}

	public String prompt() throws Exception {
		long timestamp = new Date().getTime();
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
				String message;
				while (true) {
					message = client.prompt();
					client.call(message);
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
