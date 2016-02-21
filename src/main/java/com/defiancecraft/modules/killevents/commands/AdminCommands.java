package com.defiancecraft.modules.killevents.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.components.SerialRegion;
import com.defiancecraft.modules.killevents.config.components.SerialSignWall;
import com.defiancecraft.modules.killevents.util.EventType;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class AdminCommands {

	private KillEvents plugin;
	
	public AdminCommands(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public boolean help(Player p, String[] args) {
		p.sendMessage("fuck off, use like '/killevents signwall [hourly|daily|weekly]' or '/killevents setregion' or something; I'm too tired for this shit");
		return true;
	}
	
	public boolean signWall(Player p, String[] args) {
		
		// Ensure enough args
		if (args.length < 1 || !args[0].toLowerCase().matches("hourly|daily|weekly")) {
			p.sendMessage(ChatColor.RED + "See the help, you fuckwit.");
			return true;
		}
		
		// ... and WorldEdit exists
		Plugin barePlugin;
		if ((barePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit")) == null
			|| !(barePlugin instanceof WorldEditPlugin)) {
			p.sendMessage(ChatColor.RED + "WorldEdit's not even enabled, you fuckwit.");
			return true;
		}
		
		// Get type from args
		String typeString = args[0].toLowerCase();
		EventType type = typeString.equalsIgnoreCase("hourly") ? EventType.HOURLY :
						 typeString.equalsIgnoreCase("daily") ? EventType.DAILY :
							 									EventType.WEEKLY;
		
		// Get WorldEdit & selection
		WorldEditPlugin worldEdit = (WorldEditPlugin) barePlugin;
		Selection sel;
		if ((sel = worldEdit.getSelection(p)) == null) {
			p.sendMessage(ChatColor.RED + "You don't even have a fucking selection.");
			return true;
		}
		
		try {
			SerialSignWall signWall = new SerialSignWall(sel, type);
			plugin.getConfiguration().cache.leaderboardSignWalls.add(signWall);
		} catch (Exception e) {
			p.sendMessage(String.format("%sError: %s; see console.", ChatColor.RED, e.getMessage()));
			e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean setRegion(Player p, String[] args) {
		
		// Ensure WorldEdit exists
		Plugin barePlugin;
		if ((barePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit")) == null
			|| !(barePlugin instanceof WorldEditPlugin)) {
			p.sendMessage(ChatColor.RED + "WorldEdit's not even enabled, you fuckwit.");
			return true;
		}
		
		
		// Get WorldEdit & selection
		WorldEditPlugin worldEdit = (WorldEditPlugin) barePlugin;
		Selection sel;
		if ((sel = worldEdit.getSelection(p)) == null) {
			p.sendMessage(ChatColor.RED + "You don't even have a fucking selection.");
			return true;
		}
		
		try {
			plugin.getConfiguration().pvpRegion = new SerialRegion(sel);
		} catch (Exception e) {
			p.sendMessage(String.format("%sError: %s; see console.", ChatColor.RED, e.getMessage()));
			e.printStackTrace();
		}
		
		return true;
	}
	
}
