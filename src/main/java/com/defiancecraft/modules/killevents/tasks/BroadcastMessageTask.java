package com.defiancecraft.modules.killevents.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class BroadcastMessageTask extends BukkitRunnable {

	private String message;
	
	public BroadcastMessageTask(String message) {
		this.message = message;
	}
	
	public void run() {
		Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
	}
	
}
