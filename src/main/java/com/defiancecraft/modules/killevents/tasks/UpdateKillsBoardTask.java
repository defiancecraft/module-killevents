package com.defiancecraft.modules.killevents.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.managers.KillsBoardManager;

public class UpdateKillsBoardTask extends BukkitRunnable {

	private KillEvents plugin;
	
	public UpdateKillsBoardTask(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		
		KillsBoardManager man = plugin.getKillsBoardManager();
		
		for (UUID uuid : man.getRegisteredPlayers()) {
			
			Player player = Bukkit.getPlayer(uuid);
			
			// Unregister player if they're offline
			if (player == null || !player.isOnline())
				man.unregisterPlayer(uuid);
			
			// Update scores for players -- this involves running each parser
			man.updateScores(player);
			
		}
	}
	
}
