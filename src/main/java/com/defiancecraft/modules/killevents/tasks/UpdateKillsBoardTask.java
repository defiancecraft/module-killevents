package com.defiancecraft.modules.killevents.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.managers.KillsBoardManager;
import com.defiancecraft.modules.killevents.managers.KillsBoardManager.KillsBoardVariable;

public class UpdateKillsBoardTask extends BukkitRunnable {

	private KillEvents plugin;
	
	public UpdateKillsBoardTask(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		KillsBoardManager man = plugin.getKillsBoardManager();
		for (UUID uuid : man.getRegisteredPlayers())
			updatePlayer(uuid, man);
	}
	
	public void updatePlayer(UUID uuid, KillsBoardManager man) {
		Player player;
		
		// Ensure player is online (if not, unregister them)
		if ((player = Bukkit.getPlayer(uuid)) == null || !player.isOnline()) {
			man.unregisterPlayer(uuid);
			return;
		}
		
		// Get balance (could be expensive operation) and set score
		int balance = Double.valueOf(plugin.getEconomy().getBalance(player)).intValue();
		man.setScore(player, KillsBoardVariable.TOKENS, balance);
	}
	
}
