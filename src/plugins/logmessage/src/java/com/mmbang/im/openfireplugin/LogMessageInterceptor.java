package com.mmbang.im.openfireplugin;
import java.util.Date;
import java.util.HashMap;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


public class LogMessageInterceptor implements PacketInterceptor {
	private DBCollection messages=null;
	private HashMap<String, String> excludeUsers=null;
	
	public LogMessageInterceptor(DBCollection messages, HashMap<String, String> excludeUsers){
		this.messages=messages;
		this.excludeUsers=excludeUsers;
	}
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		// TODO Auto-generated method stub
		if (packet instanceof Message && incoming && processed){
			Message msg=(Message)packet;
			String from=msg.getFrom().getNode();
			if (!this.excludeUsers.containsKey(from)){
				String to=msg.getTo().getNode();
				String content=msg.getBody();
				this.saveMessage(from, to, content);
			}
		}
	}
	
	private void saveMessage(String from, String to, String content){
		DBObject data= new BasicDBObject();
		
		data.put("from", from);
		data.put("to", to);
		data.put("content", content);
		data.put("time", new Date().getTime());
		
		this.messages.insert(data);
	}

}
