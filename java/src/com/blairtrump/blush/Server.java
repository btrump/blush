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

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author blair
 *
 */
public class Server extends NetworkCommunicator {
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
	 *            the encapsulated message
	 * @return the response of the handling subfunction as a string
	 */
	protected String handleMessage(QueueingConsumer.Delivery delivery) {
		String payload = new String(delivery.getBody());
		Packet packet = Packet.fromJson(payload);
		String response = "";
		String message = String.format("Got packet: %s", packet.toString());
		log(message);
		if (packet.isValid()) {
			try {
				if (packet.isSystem()) {
					switch (packet.getCommand()) {
					case CONNECT:
						response = connectInstance(packet);
						break;
					case DISCONNECT:
						break;
					case PASSTHROUGH:
						break;
					case PING:
						break;
					case STATUS:
						response = reportStatus();
						break;
					case TALK:
						break;
					default:
						break;
					}
				}
				// if application, automatically pass message through
			} catch (Exception e) {
//				System.err.format("[%s] %s::handleMessage() - %s\n", new Date()
//						.getTime(), this.getClass().getCanonicalName(), e.getStackTrace());
				StackTraceElement[] elements = e.getStackTrace();
				System.err.println(e.getStackTrace());
			}
		}

		return response;
	}

	/**
	 * @param packet
	 * @return
	 */
	private String connectInstance(Packet packet) {
		// not yet checking for existing instance
		// assuming new one

		// create an instance name and id
		Long timestamp = new Date().getTime();
		String instanceName = "New-Instance_" + timestamp;
		Long instanceId = timestamp;
		Instance instance = new Instance(instanceName);
		instance.connect(packet.getSenderId(), packet.getReplyQueue());
		instanceMap.put(instanceId, instance);
		String message = String.format("Connecting client '%s' (rQ: %s) to new instance '%s'\n", packet.getSenderId(), packet.getReplyQueue(), instanceName);
		log(message);
		return instanceName;
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
