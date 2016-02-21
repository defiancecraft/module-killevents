package com.defiancecraft.modules.killevents.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.KillEventsCache.PendingRewardRecord;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.config.KillEventsConfig;

public class PlayerRewarding {

	/**
	 * Attempts to reward an online player by executing `commands` for them.
	 * Commands to execute should be obtained from the configuration
	 * (see {@link EventConfig#playerCommands}), and should be given
	 * depending on the place the player came in the event.
	 * <p>
	 * If the player of UUID `uuid` is not online, this function will return false.
	 * 
	 * @param uuid UUID of player
	 * @param commands List of commands; can contain format strings (see {@link CommandUtils#formatCommand(String, UUID, int))
	 * @param kills Number of kills that player got in the event they are receiving the award for
	 * @return Whether the commands were attempted to be executed (i.e. if the player is online); note that if the commands fail, this value may still be true. 
	 */
	public static boolean rewardOnlinePlayer(UUID uuid, List<String> commands, int kills) {
		
		Player player = Bukkit.getPlayer(uuid);
		
		// Ensure player is online
		if (player == null || !player.isOnline())
			return false;

		// Execute all the commands
		for (String command : commands) {
			try {
				Bukkit.dispatchCommand(
						Bukkit.getConsoleSender(),
						CommandUtils.formatCommand(command, uuid, kills));
			} catch (CommandException e) {
				Bukkit.getLogger().warning("[KillEvents] Failed to execute command '" + command + "' for player '" + uuid + "'; stack trace below.");
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * Rewards a player who is pending reward according to the {@link KillEventsConfig#cache cache}
	 * in the config. This will remove any pending rewards that were successfully processed
	 * from the configuration.
	 * <p>
	 * If the player is not pending reward, this function will return false
	 * and do no further processing. Hence, it is safe to pass a UUID of a player
	 * that may not be pending reward.
	 * 
	 * @param plugin KillEvents plugin instance
	 * @param uuid UUID of player
	 * @return Whether the player was attempted to be rewarded, i.e. whether they had a pending reward
	 */
	public static boolean rewardPendingPlayer(KillEvents plugin, UUID uuid) {
		
		KillEventsConfig config = plugin.getConfiguration();
		
		// Ensure player has a pending reward
		if (!config.cache.pendingReward.containsKey(uuid))
			return false;

		List<PendingRewardRecord> pending = config.cache.pendingReward.get(uuid);
		List<Integer> remove = new ArrayList<>();
		
		// Process each record (for all events they haven't received playerCommands for)
		for (int i = 0; i < pending.size(); i++) {

			PendingRewardRecord record = pending.get(i);
			Map<Integer, List<String>> possibleCommands = config.getEventConfig(record.type).playerCommands;
			
			// Ensure there are commands to be executed for the place they came
			if (!possibleCommands.containsKey(record.place))
				continue;
			
			// Try and reward the player; if successful, remove this record (as it has been processed)
			if (rewardOnlinePlayer(uuid, possibleCommands.get(record.place), record.kills))
				remove.add(i);
				
		}
		
		// Remove all the processed records and update the cache
		for (int i : remove)
			pending.remove(i);
		
		config.cache.pendingReward.put(uuid, pending);
		return true;
		
	}
	
}
