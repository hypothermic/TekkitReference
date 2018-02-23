package nl.hypothermic.tekkitreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javafx.embed.swing.JFXPanel;

public class trMain extends JavaPlugin {
	
	/********************************\
	* TekkitReference by Hypothermic *
	*           trMain.java          *
	*   www.github.com/hypothermic   *
	*      admin@hypothermic.nl      *
	*---------= 21/02/2018 =---------*
	\********************************/
	
	@Override public void onEnable() {
		try {
			boolean xs = new File(getDataFolder().getPath() + File.separator + "config.yml").exists();
			defconfig(getConfig(), !xs);
		} catch (IOException e1) {
			getLogger().severe("Could not write config file correctly.");
			e1.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	// init jfx service platform
		    	new JFXPanel();
		    }
		});
		Connection sqlconn;
		try {
			getLogger().info("Connecting to database. This should not take more than 5 seconds. Timeout is 2 minutes.");
			DriverManager.setLoginTimeout(10);
			sqlconn = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("mysql-server") + "/" + 
																	getConfig().getString("mysql-database"), 
																	getConfig().getString("mysql-user") , 
																	getConfig().getString("mysql-password"));
		} catch (SQLException e) {
			getLogger().severe("[ERROR] TekkitReference could not be enabled: Connection to sql failed. Possible solutions: ");
			getLogger().severe("[ERROR] 1) Check your server address in config file");
			getLogger().severe("[ERROR] 2) Check if SQL drivers are installed (if needed)");
			getLogger().severe("[ERROR] 3) Check that the server is accessible");
			return;
		}
		this.getCommand("ref").setExecutor(new trCommandExecutor(this, sqlconn));
		this.getCommand("docs").setExecutor(new trCommandExecutor(this, sqlconn));
		getLogger().info("TekkitReference has been enabled.");
	}

	@Override public void onDisable() {
		getLogger().info("TekkitReference has been disabled.");
	}
	
	public void defconfig(FileConfiguration cfg, Boolean xs) throws IOException {
		if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
	    cfg.addDefault("mysql-server", "ext1.hypothermic.nl:3305");
	    cfg.addDefault("mysql-database", "tekkitreference");
	    cfg.addDefault("mysql-user", "tekkitreference");
	    cfg.addDefault("mysql-password", "$publicdatabase");
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
	    	result =  "# ---------- TekkitReference Configuration File ----------\n"
	    			+ "# The default mysql settings are for the Public Database\n"
	    			+ "# which is hosted on ext1.hypothermic.nl. It's recommended\n"
	    			+ "# that you host your own database for better performance.\n" + result;
	    	FileOutputStream fos = new FileOutputStream(f);
	    	fos.write(result.getBytes());
	    	fos.flush();
	    	getLogger().info("A new configuration file has been written.");
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
