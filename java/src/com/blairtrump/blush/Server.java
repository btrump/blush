/*
 * The 'lobby' is the queue on the default channel on where clients and servers initially meet
 * Client issues SYSTEM CONNECT request to server
 * Server creates new queue, representing a new instances, and passes name to client
 * Client now consumes on new instance queue
 */
package com.blairtrump.blush;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer;

public class Server extends NetworkCommunicator {
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.initialize();
		if (server.connect()) {
			server.listen();
		}
	}

	/**
	 * @param properties
	 *            An String[] of keys expected in the message. Keys absent from
	 *            the message will exist in the returned hashmap with null
	 *            values.
	 * @param message
	 *            A String containing JSON key-value pairs.
	 * @return A hashmap of keys from the properties argument with corresponding
	 *         (possibly null) values from the JSON message.
	 */
	private static Map<String, String> messageParser(String[] properties,
			String message) {
		Map<String, String> data = new HashMap<String, String>();
		JsonParser parser = new JsonParser();
		JsonObject args = parser.parse(message).getAsJsonObject();
		JsonElement element;
		for (String property : properties) {
			element = args.get(property);
			if (!(element == null)) {
				data.put(property, element.getAsString());
			} else {
				data.put(property, null);
			}
		}
		return data;
	}

	private Map<Long, Instance> instanceMap;

	public Server() {
		super();
		instanceMap = new HashMap<Long, Instance>();
	}

	public Server(NetworkCommunicator nc) {
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

	/**
	 * @param delivery
	 *            The encapsulated message.
	 * @return The response generated by the handling subfunction as a String.
	 */
	protected String handleMessage(QueueingConsumer.Delivery delivery) {
		Packet packet = new Packet(delivery);
		String response = null;
		String message = String.format("Got packet: %s", packet.toString());
		log(message);
		if (packet.isValid()) {
			try {
				if (packet.isSystem()) {
					switch (packet.getCommand()) {
					case CONNECT:
						response = handlePacketConnect(packet);
						break;
					case DISCONNECT:
						break;
					case PASSTHROUGH:
						break;
					case PING:
						break;
					case STATUS:
						response = handlePacketStatus(packet);
						break;
					case TALK:
						break;
					default:
						break;
					}
				}
				// if application, automatically pass message through
			} catch (Exception e) {
				System.err.println(e);
			}
		}

		return response;
	}

	private String handlePacketConnect(Packet packet) {
		// not yet checking for existing instance
		// assuming new one

		// create an instance name and id
		Long timestamp = new Date().getTime();
		String instanceName = "New-Instance_" + timestamp;
		Long instanceId = timestamp;
		Instance instance = new Instance(instanceName);
		// connect client to instance
		instance.connect(packet.getSenderId(), packet.getReplyQueue());
		// add instance to instance map
		instanceMap.put(instanceId, instance);
		String message = String.format(
				"Connecting client '%s' (rQ: %s) to new instance '%s'",
				packet.getSenderId(), packet.getReplyQueue(), instanceName);
		log(message);
		return instanceName;
	}

	private String handlePacketStatus(Packet packet) {
		String properties[] = { "entity", "id" };
		Map<String, String> data = messageParser(properties,
				packet.getMessage());
		String response = null;
		switch (data.get("entity")) {
		case "server":
			response = String.format("Server at %s:%s is %s on queue '%s'",
					host, port, getStatus().toString(), queue);
			break;
		case "instance":
			response = instanceMap.get(data.get("id")).toString();
			//
			break;
		default:
			break;
		}
		return response;
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
				message = String.format("Returning message: '%s'", response);
				log(message);
				channel.basicPublish("", props.getReplyTo(), replyProps,
						response.getBytes("UTF-8"));
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
}
