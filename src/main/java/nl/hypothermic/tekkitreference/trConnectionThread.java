package nl.hypothermic.tekkitreference;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

public class trConnectionThread extends Thread {
	
	public trConnectionThread(trMain cl) {
		this.cl = cl;
	}
	
	private trMain cl;
	private String v;
	
	public void run() {
		v = cl.loadVer();
		try {
			boolean xs = new File(cl.getDataFolder().getPath() + File.separator + "config.yml").exists();
			cl.defconfig(cl.getConfig(), !xs);
		} catch (IOException e1) {
			cl.getLogger().severe("Could not write config file correctly.");
			e1.printStackTrace();
		}
		Connection sqlconn = cl.connect();
		if (sqlconn == null) {
			return;
		}
		cl.getCommand("ref").setExecutor(new trCommandExecutor(cl, sqlconn, v));
		cl.getCommand("docs").setExecutor(new trCommandExecutor(cl, sqlconn,v));
		cl.getLogger().info("TekkitReference " + v + " has been enabled.");
	}
}
