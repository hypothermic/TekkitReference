package nl.hypothermic.tekkitreference;

import java.sql.Connection;

import javax.swing.SwingUtilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;

public class trCommandExecutor implements CommandExecutor {
	
	/********************************\
	* TekkitReference by Hypothermic *
	*     trCommandExecutor.java     *
	*   www.github.com/hypothermic   *
	*      admin@hypothermic.nl      *
	*---------= 21/02/2018 =---------*
	\********************************/

	public trCommandExecutor(trMain main, Connection conn) {
		this.cl = main;
		this.sqlconn = conn;
	}
	
	private final trMain cl;
	private Connection sqlconn;
	
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
				return false;
			} else {
				searchparam = ((Player) sender).getInventory().getItemInHand().getTypeId() + ":" + ((Player) sender).getInventory().getItemInHand().getData().getData();
				column = 1;
			}
		}
		if (args[0].contains("look")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command");
				return false;
			} else {
				searchparam = ((Player) sender).getTargetBlock(null, 100).getTypeId() + ":" + ((Player) sender).getTargetBlock(null, 100).getData();
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
					sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Configuration has been reloaded.");
					return true;
				}
				// more cmds
			}
			sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " Usage: /ref admin <command>");
			return true;
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
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	Bukkit.getLogger().info("Querying=" + xsearchparam + " with filter=" + xfilter + " with column=" + xcolumn);
				final trDatabaseQueryService srv = new trDatabaseQueryService(sqlconn, xsearchparam, sender, xfilter, xcolumn);
				srv.setOnFailed(new EventHandler<WorkerStateEvent>() {
		            public void handle(WorkerStateEvent workerStateEvent) {
		            	sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "[REF]" + ChatColor.RESET + ChatColor.WHITE + " An error occurred. Please contact the server administrator.");
		            	cl.getLogger().severe("Exception happened in DB Query Service: " + srv.getException().getMessage());
		            	cl.getLogger().severe("Check your connection to the MySQL database and reload the plugin. If you believe this is an error in the plugin, message the author (see readme)");
		            	cl.error("Exception happened in DB Query Service: " + srv.getException() + "\n" + srv.getException().getStackTrace());
		            }
		        });
				srv.restart();
		    }
		});
		return true;
	}
}
