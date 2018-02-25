package nl.hypothermic.tekkitreference;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class trCommandExecutor implements CommandExecutor {
	
	/********************************\
	* TekkitReference by Hypothermic *
	*     trCommandExecutor.java     *
	*   www.github.com/hypothermic   *
	*      admin@hypothermic.nl      *
	*---------= 21/02/2018 =---------*
	\********************************/

	public trCommandExecutor(trMain main, Connection conn, String ver) {
		this.cl = main;
		this.sqlconn = conn;
		this.ver = ver;
		this.lrefresh = System.currentTimeMillis() / 1000L;
	}
	
	private final trMain cl;
	private Connection sqlconn;
	private String ver;
	private long lrefresh;
	protected int[] disableditems = new int[] {136};
	
	public boolean contains(int[] array, int key) {
	    return Arrays.toString(array).matches(".*[\\[ ]" + key + "[\\],].*");
	}
	
	@Override public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
		if (// aliases
			!cmd.getName().equalsIgnoreCase("ref") && 
			!cmd.getName().equalsIgnoreCase("docs")) {
			return false;
		}
		if (args.length < 1 || args.length > 2 || 
			// player args integrity checks
			args[0].length() > 50 || 
			args[0].length() < 2 ) {
			sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Usage: /ref <search term> [filter]");
			return true;
		}
		String searchparam = args[0];
		int column = 0;
		if (args[0].contains("hand")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command");
				return true;
			} else {
				ItemStack x = ((Player) sender).getInventory().getItemInHand();
				if (contains(disableditems, x.getData().getItemTypeId())) {
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " This item can only be looked up using item id or name.");
					return true;
				}
				if (x.getData().getData() != 0) {
					searchparam = (x.getTypeId() + ":" + x.getData().getData());
				} else {
					searchparam = "" + x.getTypeId();
				}
				column = 1;
			}
		}
		if (args[0].contains("look")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command");
				return true;
			} else {
				Block x = ((Player) sender).getTargetBlock(null, 100);
				if (contains(disableditems, x.getTypeId())) {
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " This item can only be looked up using item id or name.");
					return true;
				}
				if (x.getData() != 0) {
					searchparam = x.getTypeId() + ":" + x.getData();
				} else {
					searchparam = "" + x.getTypeId();
				}
				column = 1;
			}
		}
		if (args[0].contains("admin")) {
			if (!sender.hasPermission("tekkitreference.admin")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this feature.");
				return true;
			}
			if (args.length == 2) {
				if (args[1].contains("reload")) {
					cl.reloadConfig();
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Configuration has been reloaded, now reconnecting to database...");
					try {
						sqlconn.close();
						DriverManager.setLoginTimeout(10);
						sqlconn = DriverManager.getConnection("jdbc:mysql://" + cl.getConfig().getString("mysql-server") + "/" + 
								cl.getConfig().getString("mysql-database"), 
								cl.getConfig().getString("mysql-user") , 
								cl.getConfig().getString("mysql-password"));
						cl.getLogger().info("Reload issued by " + sender.getName() + " was successful.");
						sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Successfully reconnected to database.");
					} catch (SQLException e) {
						cl.getLogger().severe("Reload issued by " + sender.getName() + ": Could not connect to database.");
						sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.RED + " Error: Could not connect to database!");
						e.printStackTrace();
					}
					return true;
				}
				if (args[1].contains("info")) {
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " TekkitReference " + ver + " by Hypothermic");
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " www.github.com/hypothermic/tekkitreference");
					return true;
				}
				// more cmds
			}
			sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Usage: /ref admin <command>");
			return true;
		}
		// ex: 151:2
		if (args[0].contains(":")) {
			String[] s = args[0].split(":");
			if (s.length != 2 || s[0] == null || s[0] == "" || s[1] == null || s[1] == "") {
				sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Example: /ref 150:8");
				return true;
			}
			column = 1;
			searchparam = args[0].trim();
		//ex 151
		} else if (args[0].replaceAll("\\D", "  ").length() == args[0].length()) {
			column = 1;
			searchparam = args[0].trim();
		}
		int filter = 0;
		if (args.length == 2) {
			if (args[1].contains("all")) {
				filter = 1;
			}
		}
		// player entered command successfully, search in db
		final int xfilter = filter;
		final String xsearchparam = searchparam;
		final int xcolumn = column;
		Bukkit.getLogger().info("debug 0");
		final boolean c = cl.getConfig().getBoolean("mute-console-output");
		try {
			if (sqlconn.isClosed()) {
				if (c) cl.getLogger().info("Connection to MySQL database is closed. Re-connecting...");
				sqlconn = cl.connect();
				if (c) cl.getLogger().info("Connected.");
			}
			if ((lrefresh + cl.getConfig().getLong("mysql-session-timeout")) < System.currentTimeMillis() / 1000L) {
				if (c) cl.getLogger().info("Refresh time has been reached. Re-connecting... " + (lrefresh + cl.getConfig().getLong("mysql-session-timeout")) + " X " + System.currentTimeMillis() / 1000L);
				lrefresh = System.currentTimeMillis() / 1000L;
	    		sqlconn = cl.connect();
	    		if (c) cl.getLogger().info("Connected.");
	    	}
		} catch (SQLException e) {
			sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " An error occurred. Please notify the server administrator.");
			cl.getLogger().info("Exception happened while trying to reconnect.");
			e.printStackTrace();
		}
		final Connection xconnection = sqlconn;
		new Thread(new Runnable() {
        	private String format(String searchparam) {
        		String out = ""; 
        	    Scanner l = new Scanner(searchparam); 
        	    while(l.hasNext()) {
        	    	String word = l.next(); 
        	    	out += Character.toUpperCase(word.charAt(0)) + word.substring(1) + " "; 
        	    }
        	    l.close();
        	    return out.trim();
        	}
        	
            public void run() { try {
            	PreparedStatement stmt;
            	if (xcolumn == 0) {
            		stmt = xconnection.prepareStatement("SELECT * FROM `refs` WHERE `name` = ?");
            	} else if (xcolumn == 1) {
            		stmt = xconnection.prepareStatement("SELECT * FROM `refs` WHERE `id` = ?");
            	} else {
            		Bukkit.getLogger().severe("Could not detect search column, searching in 'name'.");
            		stmt = xconnection.prepareStatement("SELECT * FROM `refs` WHERE `name` = ?");
            	}
            	stmt.setString(1, format(xsearchparam.replaceAll("_", " ")));
            	stmt.setQueryTimeout(cl.getConfig().getInt("mysql-query-timeout"));
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
            		return;
            	}
            	String xemc;
  		  		if (emc == null || emc == "null") {
  		  			xemc = "unknown";
  		  		} else {
  		  			xemc = emc;
  		  		}
            	// Display results depending on filter
      		  	if (xfilter == 0) {
      		  		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF] " + ChatColor.RESET + ChatColor.WHITE + name + " (" + id + ")" + ChatColor.BLUE + " [" + xemc + " EMC]");
      		  		sender.sendMessage(ChatColor.GREEN + "Mod: " + ChatColor.WHITE + xmod);
      		  		sender.sendMessage(ChatColor.GREEN + "Type: " + ChatColor.WHITE + type);
      		  		sender.sendMessage(ChatColor.GREEN + "Max stack: " + ChatColor.WHITE + maxstack);
      		  		if (type.toLowerCase().contains("Machine".toLowerCase())) {
      		  			String xsidein = sidein;
      		  			String xsideout = sideout;
      		  			String xsidepwd = sidepwd;
      		  			if (xsidein == null || xsidein == "null") {
      		  				xsidein = "N/A";
      		  			}
      		  			if (xsideout == null || xsideout == "null") {
      		  				xsideout = "N/A";
      		  			}
      		  			if (xsidepwd == null || xsidepwd == "null") {
      		  				xsidepwd = "N/A";
      		  			}
      		  			sender.sendMessage(ChatColor.GREEN + "Item input: " + ChatColor.WHITE + xsidein + ChatColor.GREEN + ", output: "+ ChatColor.WHITE + xsideout + ChatColor.GREEN + ", power: " + ChatColor.WHITE + xsidepwd);
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
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Stores max. " + icholdeu + " EU");
      		  				} else if (icrequireeu.contains("T")) {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Transfers EU");
      		  				} else {
      		  					sender.sendMessage(ChatColor.DARK_PURPLE + "- " + "Does not use EU");
      		  				}
      		  			}
      		  		}
    		  	} else if (xfilter == 1) {
    		  		sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF] " + ChatColor.RESET + ChatColor.WHITE + name + " (" + id + ")" + ChatColor.BLUE + " [" + xemc + " EMC]");
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
				return;
            } catch (Exception x) {}
		}}).start();
		return true;
	}
}
