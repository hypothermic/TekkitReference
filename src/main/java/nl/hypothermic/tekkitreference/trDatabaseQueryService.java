package nl.hypothermic.tekkitreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class trDatabaseQueryService extends Service<HashMap<String, ?>> {
	
	/********************************\
	* TekkitReference by Hypothermic *
	*   trDatabaseQueryService.java  *
	*   www.github.com/hypothermic   *
	*      admin@hypothermic.nl      *
	*---------= 23/02/2018 =---------*
	\********************************/
	
	trDatabaseQueryService(Connection connection, String searchparam, CommandSender sender, int filter, int column) {
		this.connection = connection;
		this.searchparam = searchparam;
		this.sender = sender;
		this.filter = filter;
		this.column = column;
	}
	
	private Connection connection;
	private String searchparam;
	private CommandSender sender;
	private int filter;
	private int column;
	
	// PUBLIC SQL DATABASE:
	// ext1.hypothermic.nl:3305/tekkitreference
	// usr=tekkitreference passwd=$publicdatabase
	
	// SQL COMMANDS for future reference:
	// CREATE TABLE refs (id VARCHAR(8) NOT NULL PRIMARY KEY, name VARCHAR(50), xmod VARCHAR(20), type VARCHAR(15), emc VARCHAR(9), maxstack CHAR(2), sidein VARCHAR(6), sideout VARCHAR(6), sidepwd VARCHAR(6), emitlight CHAR(1), passlight CHAR(1), icmaxcurrent CHAR(3), icrequireeu CHAR(1), rprequirebt CHAR(1), passsun CHAR(1));
	// INSERT INTO `refs` (`id`, `name`, `xmod`, `type`, `emc`, `maxstack`, `sidein`, `sideout`, `sidepwd`, `emitlight`, `passlight`, `icmaxcurrent`, `icrequireeu`, `rprequirebt`, `passsun`) VALUES ("151:2", "Accelerator", "RedPower2", "Machine", "4352", "64", "FACE", "FACE", "ANY", "Y", "Y", NULL, NULL, "Y", "Y");
	
    @Override
    protected Task<HashMap<String, ?>> createTask() {
        return new Task<HashMap<String, ?>>() {
        	public String format(String searchparam) {
        		String out = ""; 
        	    Scanner l = new Scanner(searchparam); 
        	    while(l.hasNext()) {
        	    	String word = l.next(); 
        	    	out += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " "; 
        	    }
        	    return out.trim();
        	}
        	
            @Override
            protected HashMap<String, ?> call() throws Exception {
            	PreparedStatement stmt;
            	if (column == 0) {
            		stmt = connection.prepareStatement("SELECT * FROM `refs` WHERE `name` = ?");
            	} else if (column == 1) {
            		stmt = connection.prepareStatement("SELECT * FROM `refs` WHERE `id` = ?");
            	} else {
            		Bukkit.getLogger().severe("Could not detect search column, searching in 'name'.");
            		stmt = connection.prepareStatement("SELECT * FROM `refs` WHERE `name` = ?");
            	}
            	stmt.setString(1, format(searchparam.replaceAll("_", " ")));
            	ResultSet rs = stmt.executeQuery();
            	String name = null;
            	String id = null;
            	String emc = null;
            	String xmod = null;
            	String type = null;
            	String maxstack = null;
            	String sidein = null;
            	String sideout = null;
            	String sidepwd = null;
            	String emitlight = null;
            	String passlight = null;
            	String icmaxcurrent = null;
            	String icrequireeu = null;
            	String icholdeu = null;
            	String rprequirebt = null;
            	String passsun = null;
            	while (rs.next()) {
            		name = rs.getString("name");
            		id = rs.getString("id");
            		emc = rs.getString("emc");
            		xmod = rs.getString("xmod");
            		type = rs.getString("type");
            		maxstack = rs.getString("maxstack");
            		sidein = rs.getString("sidein");
            		sideout = rs.getString("sideout");
            		sidepwd = rs.getString("sidepwd");
            		emitlight = rs.getString("emitlight");
            		passlight = rs.getString("passlight");
            		icmaxcurrent = rs.getString("icmaxcurrent");
            		icrequireeu = rs.getString("icrequireeu");
            		icholdeu = rs.getString("icholdeu");
            		rprequirebt = rs.getString("rprequirebt");
            		passsun = rs.getString("passsun");
            	}
            	if (name == null) {
            		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Could not find that item in the database.");
            		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Example: ./ref retriever");
            		return null;
            	}
            	// Display results depending on filter
      		  	if (filter == 0) {
      		  		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF] " + ChatColor.RESET + ChatColor.WHITE + name + " (" + id + ")" + ChatColor.BLUE + " [" + emc + " EMC]");
      		  		sender.sendMessage(ChatColor.GREEN + "Mod: " + ChatColor.WHITE + xmod);
      		  		sender.sendMessage(ChatColor.GREEN + "Type: " + ChatColor.WHITE + type);
      		  		sender.sendMessage(ChatColor.GREEN + "Max stack: " + ChatColor.WHITE + maxstack);
      		  		if (type.toLowerCase().contains("Machine".toLowerCase())) {
      		  			sender.sendMessage(ChatColor.GREEN + "Item input: " + ChatColor.WHITE + sidein + ChatColor.GREEN + ", output: "+ ChatColor.WHITE + sideout + ChatColor.GREEN + ", power: " + ChatColor.WHITE + sidepwd);
      		  			if (xmod.toLowerCase().contains("redpower2")) {
      		  				if (rprequirebt.contains("Y")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Requires blutricity");
      		  				} else if (rprequirebt.contains("G")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Generates blutricity");
      		  				} else if (rprequirebt.contains("S")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Stores blutricity");
      		  				} else if (rprequirebt.contains("T")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Transfers blutricity");
      		  				} else {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Does not use blutricity");
      		  				}
      		  			} else if (xmod.toLowerCase().contains("industrialcraft2")) {
      		  				if (icrequireeu.contains("Y")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Requires EU");
      		  				} else if (icrequireeu.contains("G")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Generates EU");
      		  				} else if (icrequireeu.contains("S")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Stores max." + icholdeu + " EU");
      		  				} else if (icrequireeu.contains("T")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Transfers EU");
      		  				} else {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Does not use EU");
      		  				}
      		  			}
      		  		}
    		  	} else if (filter == 1) {
    		  		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF] " + ChatColor.RESET + ChatColor.WHITE + name + " (" + id + ")" + ChatColor.BLUE + " [" + emc + " EMC]");
      		  		sender.sendMessage(ChatColor.GREEN + "Mod: " + ChatColor.WHITE + xmod);
      		  		sender.sendMessage(ChatColor.GREEN + "Type: " + ChatColor.WHITE + type);
      		  		sender.sendMessage(ChatColor.GREEN + "Max stack: " + ChatColor.WHITE + maxstack);
      		  		try {
      		  		if (!sidein.isEmpty()) {
      		  			sender.sendMessage(ChatColor.GREEN + "Item input side: " + ChatColor.WHITE + sidein);
      		  		} else {
	  					sender.sendMessage(ChatColor.GRAY + "Does not accept items. ");
	  				}} catch (NullPointerException x) {}
      		  		try {
      		  		if (!sideout.isEmpty()) {
	  					sender.sendMessage(ChatColor.GREEN + "Item outputside : " + ChatColor.WHITE + sideout);
	  				} else {
	  					sender.sendMessage(ChatColor.GRAY + "Does not output items. ");
	  				}} catch (NullPointerException x) {}
      		  		try {
	  				if (!sidepwd.isEmpty()) {
	  					sender.sendMessage(ChatColor.GREEN + "Power side: " + ChatColor.WHITE + sidepwd);
	  				} else {
	  					sender.sendMessage(ChatColor.GRAY + "Does not accept power. ");
	  				}} catch (NullPointerException x) {}
	  				try {
      		  		if (rprequirebt.contains("Y")) {
	  					sender.sendMessage(ChatColor.DARK_PURPLE + "- Requires blutricity");
	  				} else if (rprequirebt.contains("G")) {
	  					sender.sendMessage(ChatColor.DARK_PURPLE + "- Generates blutricity");
	  				} else if (rprequirebt.contains("S")) {
		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Stores blutricity");
		  			} else if (rprequirebt.contains("T")) {
		  			// 'only' because all machines which use blutricity transmit it
		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Only transfers blutricity");
		  			} else {
	  					sender.sendMessage(ChatColor.GRAY + "- Does not use blutricity");
	  				}} catch (NullPointerException x) { sender.sendMessage(ChatColor.GRAY + "- Does not use blutricity"); }
      		  		try {
		  			if (icrequireeu.contains("Y")) {
  		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Requires EU");
  		  			} else if (icrequireeu.contains("G")) {
  		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Generates EU");
  		  			} else if (icrequireeu.contains("S")) {
		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Stores max." + icholdeu + " EU");
		  			} else if (icrequireeu.contains("T")) {
		  				sender.sendMessage(ChatColor.DARK_PURPLE + "- Transfers EU");
		  			} else {
  		  				sender.sendMessage(ChatColor.GRAY + "- Does not use EU");
  		  			}} catch (NullPointerException x) { sender.sendMessage(ChatColor.GRAY + "- Does not use EU"); }
      		  		try {
		  			if (emitlight.contains("Y")) {
		  				sender.sendMessage(ChatColor.YELLOW + "- Emits light");
		  			} else {
		  				sender.sendMessage(ChatColor.GRAY + "- Does not emit light");
		  			}} catch (NullPointerException x) {}
      		  		try {
		  			if (passlight.contains("Y")) {
		  				sender.sendMessage(ChatColor.YELLOW + "- Light can pass through block");
		  			} else {
		  				sender.sendMessage(ChatColor.GRAY + "- Light can't pass through block");
		  			}} catch (NullPointerException x) {}
      		  		try {
		  			if (passsun.contains("Y")) {
		  				sender.sendMessage(ChatColor.YELLOW + "- Sunlight can pass through block");
		  			} else {
		  				sender.sendMessage(ChatColor.GRAY + "- Sunlight can't pass through block");
		  			}} catch (NullPointerException x) {}
    		  	}
				return null;
            }
        };
        
    	/* // old format function in case we need it again
    	 * public String format(String original) {
    	 *   if (original == null || original.length() == 0) {
    	 *       return original;
    	 *   }
    	 *   return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    	}*/
    }
}