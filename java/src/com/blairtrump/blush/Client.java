package com.blairtrump.blush;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.util.Date;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class Client extends NetworkCommunicator {
	public static void main(String[] argv) {
		Client client = null;
		try {
			client = new Client();
			client.initialize();
			if (client.connect()) {
				String message;
				while (true) {
					message = client.prompt();
					String[] messageArray = message.split(":");
					String args = null;
					try {
						int packetId = Integer.parseInt(messageArray[0]);
						if(messageArray.length > 1) {
							args = messageArray[1];
						}
						client.handleCommand(packetId, args);
					} catch (Exception e) {
						errorlog("Invalid command format", e);
					}
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

	public String call(Packet packet) throws Exception {
		String response = null;
		String corrId = UUID.randomUUID().toString();
		Date timestamp = new Date();
		BasicProperties props = new BasicProperties.Builder()
				.timestamp(timestamp).correlationId(corrId).replyTo(replyQueue)
				.build();
		System.out.format("\t[>] Sending packet '%s'\n",
				packet.toJson());
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

	public Packet buildPacketConnect() {
		String message = null;
		Packet.Type type = Packet.Type.SYSTEM;
		Packet.Command command = Packet.Command.CONNECT;
		Boolean valid = true;
		Packet packet = new Packet(type, message, command, getId());
		packet.setValidity(valid);
		return packet;
	}

	public void handleCommand(int i, String arg) throws Exception {
		switch (i) {
		case 0:
			// Connect
			call(buildPacketConnect());
			break;
		case 1:
			// Query Server
			call(buildPacketStatusServer());
			break;
		case 2:
			// Query all Instances
			call(buildPacketStatusInstances());
			break;
		case 3:
			// Query Instance
			call(buildPacketStatusInstance(Long.parseLong(arg)));
			break;
		default:
			break;
		}
	}

	public Packet buildPacketStatusServer() {
		String message = null;
		Packet.Type type = Packet.Type.SYSTEM;
		Packet.Command command = Packet.Command.STATUS;
		Boolean valid = true;
		Properties properties = new Properties();
		properties.put("entity", "server");
		message = properties.toString();
		Packet packet = new Packet(type, message, command, getId());
		packet.setValidity(valid);
		return packet;
	}

	public Packet buildPacketStatusInstances() { 
		String message = null;
		Packet.Type type = Packet.Type.SYSTEM;
		Packet.Command command = Packet.Command.STATUS;
		Boolean valid = true;
		Properties properties = new Properties();
		properties.put("entity", "instance");
		message = properties.toString();
		Packet packet = new Packet(type, message, command, getId());
		packet.setValidity(valid);
		return packet;
	}
	
	public Packet buildPacketStatusInstance(Long instanceId) {
		String message = null;
		Packet.Type type = Packet.Type.SYSTEM;
		Packet.Command command = Packet.Command.STATUS;
		Boolean valid = true;
		Properties properties = new Properties();
		properties.put("entity", "instance");
		properties.put("id", instanceId);
		message = properties.toString();
		Packet packet = new Packet(type, message, command, getId());
		packet.setValidity(valid);
		return packet;
	}
	
	public Packet buildPacketTalk(String s) {
		String message = null;
		Packet.Type type = Packet.Type.SYSTEM;
		Packet.Command command = Packet.Command.TALK;
		Boolean valid = true;
		Properties properties = new Properties();
		properties.put("message", s);
		message = properties.toString();
		Packet packet = new Packet(type, message, command, getId());
		packet.setValidity(valid);
		return packet;
	}

	public String prompt() throws Exception {
		long timestamp = new Date().getTime();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String prompt = String.format("[%s] blush.Client~# ", timestamp);
		System.out.print(prompt);
		return reader.readLine();
	}
}
