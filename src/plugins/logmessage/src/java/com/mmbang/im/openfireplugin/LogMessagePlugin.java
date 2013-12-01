package com.mmbang.im.openfireplugin;
import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class LogMessagePlugin implements Plugin {
    private PacketInterceptor ic=null;
	private Mongo mongo=null;
	private PluginManager manager=null;

	private String host="192.168.1.51";
	private int port=27017;
	private String dbName="openfire";
	private String colName="messages";	
	private HashMap<String, String> excludeUsers=new HashMap<String, String>();
	
	public void destroyPlugin() {
		// TODO Auto-generated method stub
		System.out.println("destroy LogMessage plugin...");
		if(ic!=null){
			InterceptorManager.getInstance().removeInterceptor(ic);
		}
		this.closeConn();
	}

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		// TODO Auto-generated method stub
		System.out.println("start LogMessage plugin...");
		this.manager=manager;
		System.out.println("begin load config");
		this.loadConfig();
		System.out.println("load ok");
		ic=new LogMessageInterceptor(this.getCollection(), this.excludeUsers);
		InterceptorManager.getInstance().addInterceptor(ic);
	}
	
	private void closeConn()
	{
		if(mongo!=null){
			mongo.close();
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
			System.out.println("dfdf");
			System.out.println(doc+"2233");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("111");
		}
		System.out.println("222");
		
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
		
		System.out.println("dfdf");
		
		NodeList excludeEl=doc.getElementsByTagName("excludeUserIds");
		org.w3c.dom.Node excludeNode=excludeEl.item(0);
		NodeList excludeConfNodes=excludeNode.getChildNodes();
		for(int j=0; j<excludeConfNodes.getLength(); j++){
			String nodeName=excludeConfNodes.item(j).getNodeName();
			String nodeText=excludeConfNodes.item(j).getTextContent();
			if("userId".equals(nodeName)){
				this.excludeUsers.put(nodeText.trim(), "yes");
				System.out.println(nodeText);
			}
		}
		System.out.println("excludeUsers:"+this.excludeUsers);
	}
	
	private DBCollection getCollection()
	{
		try {
			mongo=new Mongo(host, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mongo.getDB(dbName).getCollection(colName);
	}

}
