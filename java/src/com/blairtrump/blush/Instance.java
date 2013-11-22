package com.blairtrump.blush;

import java.util.HashMap;
import java.util.Map;

public class Instance {
	private String name;
	private Map<Long, String> clientReplyQueues;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Long, String> getClientReplyQueues() {
		return clientReplyQueues;
	}

	public void setClientReplyQueues(Map<Long, String> clientReplyQueues) {
		this.clientReplyQueues = clientReplyQueues;
	}

	public Instance(String name) {
		this.name = name;
		clientReplyQueues = new HashMap<Long, String>();
	}

	public boolean connect(Long clientId, String replyQueue) {
		boolean success = true;
		try {
			clientReplyQueues.put(clientId, replyQueue);
		} catch (Exception ignore) {
			System.err.println("e");
			success = false;
		}
		return success;
	}

	public String toString() {
		return String.format("%s with %s queue%s", name, clientReplyQueues
				.size(), clientReplyQueues.size() > 1 ? "s" : "");
	}
}
