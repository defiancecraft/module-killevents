package com.defiancecraft.modules.killevents.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;

public class UpdateLeaderboardSignsTask extends BukkitRunnable {

	private KillEvents plugin;
	
	public UpdateLeaderboardSignsTask(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		plugin.getLeaderboardSignManager().updateSigns();
	}
			
}
