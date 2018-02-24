package nl.hypothermic.tekkitreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class trMain extends JavaPlugin {
	
	/********************************\
	* TekkitReference by Hypothermic *
	*           trMain.java          *
	*   www.github.com/hypothermic   *
	*      admin@hypothermic.nl      *
	*---------= 21/02/2018 =---------*
	\********************************/
	
	protected Connection connect() {
		Connection sqlconn;
		try {
			if (getConfig().getBoolean("mute-console-output"));
			getLogger().info("Connecting to database. This should not take more than 5 seconds. Timeout is 2 minutes.");
			DriverManager.setLoginTimeout(getConfig().getInt("mysql-connect-timeout"));
			sqlconn = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("mysql-server") + "/" + 
																	getConfig().getString("mysql-database"), 
																	getConfig().getString("mysql-user") , 
																	getConfig().getString("mysql-password"));
			return sqlconn;
		} catch (SQLException e) {
			getLogger().severe("[ERROR] TekkitReference could not be enabled: Connection to sql failed. Possible solutions: ");
			getLogger().severe("[ERROR] 1) Check your server address in config file");
			getLogger().severe("[ERROR] 2) Check if SQL drivers are installed (if needed)");
			getLogger().severe("[ERROR] 3) Check that the server is accessible");
			return null;
		}
	}
	
	@Override public void onEnable() {
		loadVer();
		try {
			boolean xs = new File(getDataFolder().getPath() + File.separator + "config.yml").exists();
			defconfig(getConfig(), !xs);
		} catch (IOException e1) {
			getLogger().severe("Could not write config file correctly.");
			e1.printStackTrace();
		}
		Connection sqlconn = connect();
		if (sqlconn == null) {
			return;
		}
		this.getCommand("ref").setExecutor(new trCommandExecutor(this, sqlconn, v));
		this.getCommand("docs").setExecutor(new trCommandExecutor(this, sqlconn,v));
		getLogger().info("TekkitReference " + v + " has been enabled.");
	}

	@Override public void onDisable() {
		getLogger().info("TekkitReference has been disabled.");
	}
	
	private String v;
	
	public void defconfig(FileConfiguration cfg, Boolean xs) throws IOException {
		if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
	    cfg.addDefault("mysql-server", "ext1.hypothermic.nl:3305");
	    cfg.addDefault("mysql-database", "tekkitreference");
	    cfg.addDefault("mysql-user", "tekkitreference");
	    cfg.addDefault("mysql-password", "$publicdatabase");
	    cfg.addDefault("mysql-connect-timeout", 20);
	    cfg.addDefault("mysql-query-timeout", 10);
	    cfg.addDefault("mysql-session-timeout", 120L);
	    cfg.addDefault("mute-console-output", false);
	    cfg.options().copyDefaults(true);
	    saveConfig();
	    if (xs) {
	    	File f = new File(getDataFolder().getPath() + File.separator + "config.yml");
	    	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	    	String result = "";
	    	String line = "";
	    	while( (line = br.readLine()) != null){
	    		result = result + line + "\n"; 
	    	}
	    	result =  "# ----- TekkitReference Configuration (v" + v + ")-----\n"
	    			+ "# The default mysql settings are for the Public Database\n"
	    			+ "# which is hosted on ext1.hypothermic.nl. It's recommended\n"
	    			+ "# that you host your own database for better performance.\n" + result;
	    	FileOutputStream fos = new FileOutputStream(f);
	    	fos.write(result.getBytes());
	    	fos.flush();
	    	fos.close();
	    	getLogger().info("A new configuration file has been written.");
	    	br.close();
	    }
	}
	
	public void loadVer() {
		try {
			v = this.getDescription().getVersion();
		} catch (Exception x) {
			v = "[unknown]";
		}
		if (v == null) {
			v = "[unknown]";
		}
	}
	
    public void error(String message) {
        try {
            if(!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            if (!new File(getDataFolder(), "error.log").exists()){
            	new File(getDataFolder(), "error.log").createNewFile();
            }
            PrintWriter pw = new PrintWriter(new FileWriter(new File(getDataFolder(), "error.log"), true));
            pw.println(message);
            pw.flush();
            pw.close();
 
        } catch (IOException e) {
            getLogger().severe("Could not access the error log.");
    }}
}
