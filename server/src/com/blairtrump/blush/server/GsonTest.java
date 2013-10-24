package com.blairtrump.blush.server;

import com.google.gson.Gson;

public class GsonTest {
	public static void main(String [] args) throws Exception {
		String json1 = 
				"{"
					+ "'py/object': '__main__.Packet',"
					+ "'packet_type': 'new_session',"
					+ "'client_ids': 'String',"
					+ "'session_id': 'String'"
				+ "}";
        String json2 = 
                "{"
                    + "'title': 'Computing and Information systems',"
                    + "'id' : 1,"
                    + "'children' : 'true',"
                    + "'groups' : [{"
                        + "'title' : 'Level one CIS',"
                        + "'id' : 2,"
                        + "'children' : 'true',"
                        + "'groups' : [{"
                            + "'title' : 'Intro To Computing and Internet',"
                            + "'id' : 3,"
                            + "'children': 'false',"
                            + "'groups':[]"
                        + "}]" 
                    + "}]"
                + "}";
        TestPacket packet = new Gson().fromJson(json1, TestPacket.class);
		
		System.out.println(packet);
	}
}

class TestPacket {
	private String packet_type;
	private int session_id;
	private int[] client_ids;
}