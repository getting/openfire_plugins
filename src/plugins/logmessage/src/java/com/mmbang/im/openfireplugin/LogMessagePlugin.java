package com.mmbang.im.openfireplugin;
/**
 * ant -f build.xml -Dplugin=logmessage plugin
 */
import java.io.File;
import java.net.UnknownHostException;

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
		this.loadConfig();
		ic=new LogMessageInterceptor(this.getCollection());
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
