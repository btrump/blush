package com.blairtrump.blush;

import java.util.HashMap;
import java.util.Map;

public class Instance {
	String name;
	Map<Long, String> clientReplyQueues;
	
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
}
