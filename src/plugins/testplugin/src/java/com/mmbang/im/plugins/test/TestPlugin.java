package com.mmbang.im.plugins.test;

import java.io.File;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;

public class TestPlugin implements Plugin {

	@Override
	public void destroyPlugin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializePlugin(PluginManager arg0, File arg1) {
		// TODO Auto-generated method stub
		System.out.println("start test plugin...");
	}

}
