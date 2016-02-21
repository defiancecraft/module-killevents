package com.defiancecraft.modules.killevents.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.tracker.KillTracker;
import com.defiancecraft.modules.killevents.util.EventType;
import com.defiancecraft.modules.killevents.util.PlayerRewarding;

public class PlayerListener implements Listener {

	private KillEvents plugin;
	
	public PlayerListener(KillEvents plugin) {
		this.plugin = plugin;
	}

	// Handle kill event to add kills to tracker and update
	// the scoreboard for that player
	@EventHandler
	public void onPlayerKill(PlayerDeathEvent e) {
		
		Player killer = e.getEntity().getKiller();
		
		// Ensure they were killed by a player
		if (killer == null)
			return;
		
		// ... and that they are in the defined region
		if (!plugin.getConfiguration().pvpRegion.toSelection().contains(killer.getLocation()))
			return;
		
		// Add kills for killer
		KillTracker tracker = plugin.getTracker();
		tracker.setKills(killer.getUniqueId(), EventType.HOURLY, tracker.getKills(killer.getUniqueId(), EventType.HOURLY) + 1);
		tracker.setKills(killer.getUniqueId(), EventType.DAILY, tracker.getKills(killer.getUniqueId(), EventType.DAILY) + 1);
		tracker.setKills(killer.getUniqueId(), EventType.WEEKLY, tracker.getKills(killer.getUniqueId(), EventType.WEEKLY) + 1);
		
		// Update scoreboard
		plugin.getKillsBoardManager().updatePlayer(killer, false);
		
		// Update sign walls
		
		
	}
	
	// Handle join event to show scoreboard to players and reward them
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent e) {

		if (plugin.getConfiguration().pvpRegion != null
				&& e.getPlayer().getWorld().getName().equalsIgnoreCase(plugin.getConfiguration().pvpRegion.a.world)) {
			// Show scoreboard for player
			plugin.getKillsBoardManager().registerPlayer(e.getPlayer());
			plugin.getKillsBoardManager().updatePlayer(e.getPlayer(), true);
		}
		
		// Attempt to reward player if they have pending reward
		UUID uuid = e.getPlayer().getUniqueId();
		PlayerRewarding.rewardPendingPlayer(plugin, uuid);

	}
	
	// Handle player changing world to remove the scoreboard
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
		
		if (plugin.getConfiguration().pvpRegion == null)
			return;
		
		// Remove scoreboard if player is going from scoreboard-enabled world and registered
		if (e.getFrom().getName().equalsIgnoreCase(plugin.getConfiguration().pvpRegion.a.world)
				&& plugin.getKillsBoardManager().isPlayerRegistered(e.getPlayer().getUniqueId())) {
			
			plugin.getKillsBoardManager().unregisterPlayer(e.getPlayer());
			
		// Add/show scoreboard if player is going to scoreboard-enabled world and is not registered
		} else if (e.getPlayer().getWorld().getName().equalsIgnoreCase(plugin.getConfiguration().pvpRegion.a.world)
				&& !plugin.getKillsBoardManager().isPlayerRegistered(e.getPlayer().getUniqueId())) {
			
			plugin.getKillsBoardManager().registerPlayer(e.getPlayer());
			plugin.getKillsBoardManager().updatePlayer(e.getPlayer(), true);
			
		}
		
	}
	
}
