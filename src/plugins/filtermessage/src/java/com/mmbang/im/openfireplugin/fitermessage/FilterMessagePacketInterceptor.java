package com.mmbang.im.openfireplugin.fitermessage;

import java.util.HashMap;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import com.danga.MemCached.MemCachedClient;

public class FilterMessagePacketInterceptor implements PacketInterceptor {
	private MemCachedClient mc=null;
	private HashMap<String, String> excludeUsers=new HashMap<String, String>();
	public FilterMessagePacketInterceptor(MemCachedClient mc, HashMap<String, String> excludeUsers){
		this.mc=mc;
		this.excludeUsers=excludeUsers;
	}
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		// TODO Auto-generated method stub
		Message message=(Message)packet;
		String to=message.getTo().getNode();
		String from=message.getFrom().getNode();
		System.out.println(this.inBlackList(to, from)==true);
		if(!this.excludeUsers.containsKey(from)){
			if(this.inBlackList(to, from)){
				System.out.println("inblacklist\tto:"+to+"\tfrom"+from);
				throw new PacketRejectedException();
			}			
			if(!this.isSameVillage(to, from) && !this.inFriend(to, from)){
				System.out.println("isSameVillage\tto:"+to+"\tfrom"+from);
				throw new PacketRejectedException();
			}			
		}
	}
	
	private boolean isSameVillage(String to, String from)
	{
		boolean result=false;
		String toVillage=(String) this.mc.get(to);
		String fromVillage=(String) this.mc.get(from);
		if(toVillage.equals(fromVillage)){
			result=true;
		}
		return result;
	}
	
	private boolean inFriend(String to, String from){
		boolean result=false;
		String memKey=from+"-"+to;
		String memVal=(String) this.mc.get(memKey);
		if("friend".equals(memVal)){
			result=true;
		}
		return result;
	}
	
	private boolean inBlackList(String to, String from){
		boolean result=false;
		String memKey=from+"-"+to;
		System.out.println("filter key:"+memKey);
		String memVal=(String) this.mc.get(memKey);
		System.out.println("filter value:"+memVal);
		if("blacklist".equals(memVal)){
			result=true;
		}
		System.out.println("result:"+result);
		return result;
	}
}
