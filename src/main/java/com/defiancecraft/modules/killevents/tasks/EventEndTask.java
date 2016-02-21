package com.defiancecraft.modules.killevents.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.KillEventsCache;
import com.defiancecraft.modules.killevents.config.KillEventsCache.PendingRewardRecord;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.util.CommandUtils;
import com.defiancecraft.modules.killevents.util.EventType;
import com.defiancecraft.modules.killevents.util.PlayerRewarding;

public class EventEndTask extends BukkitRunnable {

	protected KillEvents plugin;
	protected long lastTimeMillis;
	protected boolean hasExecuted = false;
	
	public EventEndTask(KillEvents plugin) {
		this.plugin = plugin;
		this.lastTimeMillis = System.currentTimeMillis();
	}
	
	public void run() {

		this.hasExecuted = true;
		this.lastTimeMillis = System.currentTimeMillis();
		
		// Run hourly event (this task is hourly)
		endEvent(EventType.HOURLY);
		
		// Increment daily hours and run if 24
		if (++plugin.getConfiguration().cache.dailyHours >= 24)
			endEvent(EventType.DAILY);
		
		// Increment weekly hours and run if 168 (24 * 7)
		if (++plugin.getConfiguration().cache.weeklyHours >= 24 * 7)
			endEvent(EventType.WEEKLY);
		
	}
	
	public void shutdown() {
		
		long difference = System.currentTimeMillis() - lastTimeMillis;
		int ticks = (int)difference / 50;
		int hourlyElapsed = ticks;

		// If we have not yet executed, add the previous config time on 
		if (!hasExecuted)
			hourlyElapsed += plugin.getConfiguration().cache.hourlyElapsed;
		
		if (hourlyElapsed >= EventType.HOURLY.getTicks()) {
			Bukkit.getLogger().warning("Timing inconsistency detected! Hourly check took longer than expected. Future timing may not be aligned to current event timing.");
			hourlyElapsed %= EventType.HOURLY.getTicks();
		}
		
		plugin.getConfiguration().cache.hourlyElapsed = hourlyElapsed;
		
	}
	
	protected void endEvent(EventType type) {
		
		// Get the appropriate event config
		EventConfig eventConfig = plugin.getConfiguration().getEventConfig(type);
		
		// Ensure event is configured
		if (eventConfig == null) {
			plugin.getLogger().severe(String.format("Event %s is not configured; please create a configuration for the event. Plugin will be disabled.", type.name()));
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		
		// Get players that placed
		List<Entry<UUID, Integer>> placements = plugin.getTracker().getEventKills(type)
			.entrySet()
			.stream()
			.sorted((a, b) -> a.getValue().compareTo(b.getValue())) // Sort
			.collect(Collectors.toList()); // Collect as ordered list
		
		// Reset event kills (we already have the kills stored)
		plugin.getTracker().resetEventKills(type);
		
		// Sequentially iterate over each place
		for (int place = 0; place < placements.size(); place++) {
			
			Entry<UUID, Integer> entry = placements.get(place);
			
			int canonicalPlace = place + 1; 	   // Player's place in canonical format (e.g. 1 for 1st instead of 0)
			int kills          = entry.getValue(); // Player's achieved number of kills in this event
			UUID uuid          = entry.getKey();   // UUID of player
			
			// Execute serverCommands (if any)
			for (String command : eventConfig.serverCommands.getOrDefault(canonicalPlace, Collections.<String>emptyList())) {
				
				try {
					Bukkit.dispatchCommand(
						Bukkit.getConsoleSender(),
						CommandUtils.formatCommand(command, uuid, kills));
				} catch (CommandException e) {
					Bukkit.getLogger().warning("[KillEvents] An error occurred executing serverCommand '" + command + "' for player UUID '" + uuid + "'; stack trace below.");
					e.printStackTrace();
				}
				
			}
			
			Player player = Bukkit.getPlayer(entry.getKey());
			
			// Attempt to reward player (execute playerCommands) if they are online;
			// otherwise, add them to the pending reward list in the config cache.
			if (!PlayerRewarding.rewardOnlinePlayer(
					player.getUniqueId(),
					eventConfig.playerCommands.getOrDefault(canonicalPlace, Collections.<String>emptyList()),
					kills)) {
				
				// Wrap in ArrayList in case of immutable implementation
				List<PendingRewardRecord> records = new ArrayList<>(
						plugin.getConfiguration().cache.pendingReward.getOrDefault(
							uuid,
							Collections.<PendingRewardRecord>emptyList()
						)
				);
				
				records.add(new KillEventsCache.PendingRewardRecord(type, kills, canonicalPlace));
				plugin.getConfiguration().cache.pendingReward.put(uuid, records);
				
			}
			
		}
		
		// Yeah, fuck it, we might as well update all the leaderboard sign walls
		plugin.getLeaderboardSignManager().updateSigns();
		
		// Reset timer for the event
		switch (type) {
		case HOURLY:
			plugin.getConfiguration().cache.hourlyElapsed = 0;
			break;
		case DAILY:
			plugin.getConfiguration().cache.dailyHours = 0;
			break;
		case WEEKLY:
			plugin.getConfiguration().cache.weeklyHours = 0;
			break;
		}
		
	}
	
}
