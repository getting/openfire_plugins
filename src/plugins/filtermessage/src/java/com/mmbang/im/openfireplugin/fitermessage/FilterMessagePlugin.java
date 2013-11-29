package com.mmbang.im.openfireplugin.fitermessage;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.mongodb.*;

public class FilterMessagePlugin implements Plugin {
	private String host="192.168.1.51";
	private int port=27017;
	private String dbName="area";
	private String colName="user_fans";	
	private PacketInterceptor ic=null;
	private PluginManager manager=null;
	private Mongo mongo=null;
	private MemCachedClient mc=null;
	private String[] memcacheList=null;

	public void destroyPlugin() {
		// TODO Auto-generated method stub
		if(ic!=null){
			InterceptorManager.getInstance().removeInterceptor(ic);
		}
	}

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		// TODO Auto-generated method stub
		System.out.println("filter message plugin");
		this.manager=manager;
		this.loadConfig();
		
		this.initMongo();		
		this.initMemcache();
		
		this.loadUsersVillage();
		this.loadFans();
		System.out.println("1");

		ic =new FilterMessagePacketInterceptor(mc);
		InterceptorManager.getInstance().addInterceptor(ic);
	}
	
	private void initMongo()
	{
		try {
			this.mongo=new Mongo(host, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initMemcache()
	{
        String[] serverlist = this.memcacheList;  
        SockIOPool pool = SockIOPool.getInstance();  
        pool.setServers(serverlist);  
        pool.initialize();
        System.out.println("2");
		this.mc=new MemCachedClient();
		System.out.println("3");		
	}
	
	private void loadUsersVillage()
	{
		DBCursor users=this.mongo.getDB("area").getCollection("user").find();
		System.out.println(this.mongo);
		while(users.hasNext()){
			System.out.println("v1");
			BasicDBObject user=(BasicDBObject) users.next();
			System.out.println("v2");
			String userId=user.getObjectId("_id").toString();
			System.out.println("v3");
			String village=user.getObjectId("village").toString();
			System.out.println("2user_info\t"+userId+":"+village);
			if(userId==null || village==null){
				continue;
			}
			System.out.println("user_info\t"+userId+":"+village);
			
			this.mc.set(userId, village);
		}
	}
	
	private void loadFans()
	{
		DBCursor fans=this.mongo.getDB(dbName).getCollection(colName).find();
		System.out.println("4");
		while(fans.hasNext()){
			BasicDBObject userFan=(BasicDBObject) fans.next();
			String userKey=userFan.getString("user");
			
			//blacklits
			BasicDBList blacklist=(BasicDBList) userFan.get("blacklist");
			if(blacklist==null){
				continue;
			}
			Object[] blackArray=blacklist.toArray();
			int len=blackArray.length;
			for (int i=0; i<len; i++){
				String memKey=blackArray[i]+"-"+userKey;
				String memVal="blacklist";
				System.out.println(memVal+"\t"+memKey+":"+memVal);
				this.mc.set(memKey, memVal, 0);
			}
			
			//friends
			BasicDBList friendlist=(BasicDBList) userFan.get("friends");
			if(friendlist==null){
				continue;
			}
			Object[] friendsArray=friendlist.toArray();
			for(int j=0; j<friendsArray.length; j++){
				String memKey=friendsArray[j]+"-"+userKey;
				String memVal="friend";
				System.out.println(memVal+"\t"+memKey+":"+memVal);
				this.mc.set(memKey, memVal, 0);
			}
		}
	}
	
	private void loadConfig()
	{
		String xmlPath=manager.getPluginDirectory(this)+"/plugin.xml";
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db=null;
		Document doc=null;
		try {
			db=dbf.newDocumentBuilder();
			doc = db.parse(new File(xmlPath));
			System.out.println(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NodeList el=doc.getElementsByTagName("mongo");
		org.w3c.dom.Node mongoNode = el.item(0);
		NodeList mongoConfNodes=mongoNode.getChildNodes();
		for (int i=0; i<mongoConfNodes.getLength(); i++){
			String nodeName=mongoConfNodes.item(i).getNodeName();
			String nodeText=mongoConfNodes.item(i).getTextContent();
			if("host".equals(nodeName))
			{
				this.host=nodeText;
			} else if("port".equals(nodeName)){
				this.port=new Integer(nodeText);
			} else if("db".equals(nodeName)){
				this.dbName=nodeText;
			} else if("collection".equals(nodeName)){
				this.colName=nodeText;
			}
		}
		
		ArrayList<String> memcachedHosts=new ArrayList<String>();
		NodeList memEl=doc.getElementsByTagName("memcached");
		org.w3c.dom.Node memcacheNode=memEl.item(0);
		NodeList memcacheHostNodes=memcacheNode.getChildNodes();
		for (int j=0; j<memcacheHostNodes.getLength(); j++){
			String memNodeName=memcacheHostNodes.item(j).getNodeName();
			String memNodeValue=memcacheHostNodes.item(j).getTextContent();
			if("host".equals(memNodeName)){
				memcachedHosts.add(memNodeValue);
			}
		}
		
		Object[] ObjectList=memcachedHosts.toArray();
		this.memcacheList=Arrays.copyOf(ObjectList, ObjectList.length, String[].class);
		System.out.println(this.memcacheList[0]);
	}	
}
