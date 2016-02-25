package com.defiancecraft.modules.killevents.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;
import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.KillEventsConfig;
import com.defiancecraft.modules.killevents.tasks.UpdateKillsBoardTask;
import com.defiancecraft.modules.killevents.util.EventType;

public class KillsBoardManager  {

	private static final String OBJECTIVE_NAME = "KillEvents";
	
	// Value for field 'Position' for sidebar position of packet 0x3D (display scoreboard) 
	private static final int DISPLAY_POSITION_SIDEBAR = 1;
	
	// Reference to plugin
	private KillEvents plugin;
	
	// Update board task
	private UpdateKillsBoardTask updateTask;
	
	// List of players for whom a task to update their money is running
	// (for individuals triggered with #updatePlayer; `updateTask` handles
	// everyone)
	private List<UUID> updatingMoney = new ArrayList<>();
	
	// List of registered players (i.e. those who will be shown the scoreboard)
	private Set<UUID> registeredPlayers = new HashSet<>();
	
	public KillsBoardManager(KillEvents plugin) {
		this.plugin = plugin;
		this.updateTask = new UpdateKillsBoardTask(plugin);
		this.updateTask.runTaskTimerAsynchronously(plugin, 0, plugin.getConfiguration().scoreboard.frequencyTicks);
	}
	
	/**
	 * Registers a player and shows them the scoreboard
	 * @param player Player to register
	 */
	public void registerPlayer(Player player) {
		
		// Create packet to create objective
		WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective();
		objective.setName(OBJECTIVE_NAME);
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfiguration().scoreboard.title));
		objective.setMode(WrapperPlayServerScoreboardObjective.Mode.ADD_OBJECTIVE);

		// Create packet to show objective
		WrapperPlayServerScoreboardDisplayObjective displayObjective = new WrapperPlayServerScoreboardDisplayObjective();
		displayObjective.setPosition(DISPLAY_POSITION_SIDEBAR);
		displayObjective.setScoreName(OBJECTIVE_NAME);
		
		objective.sendPacket(player);
		displayObjective.sendPacket(player);
	
		// Add them to registered players
		if (!this.registeredPlayers.contains(player.getUniqueId()))
			this.registeredPlayers.add(player.getUniqueId());
		
		
		// Set scores
		setScore(player, KillsBoardVariable.HOURLY_KILLS, 0);
		setScore(player, KillsBoardVariable.DAILY_KILLS, 0);
		setScore(player, KillsBoardVariable.WEEKLY_KILLS, 0);
		setScore(player, KillsBoardVariable.TOKENS, 0);
		
	}

	/**
	 * Unregisters a player and unregisters the objective for the scoreboard
	 * (and so hides the scoreboard)
	 * 
	 * @param player Player to unregister
	 */
	public void unregisterPlayer(Player player) {
		Scoreboard board = player.getScoreboard();
		Objective objective;
		
		if (board != null && (objective = board.getObjective(OBJECTIVE_NAME)) != null)
			objective.unregister();
	
		this.registeredPlayers.remove(player.getUniqueId());
	}
	
	/**
	 * Unregisters a player and, if they are online, unregisters the objective
	 * for the player (and so hides the scoreboard).
	 * 
	 * @param uuid UUID of player
	 */
	public void unregisterPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null && player.isOnline())
			unregisterPlayer(player);
		else
			this.registeredPlayers.remove(uuid);
	}
	
	/**
	 * Gets a set of registered players
	 * @return Set of registered players
	 */
	public Set<UUID> getRegisteredPlayers() {
		return new HashSet<UUID>(registeredPlayers);
	}
	
	/**
	 * Checks if a player is registered
	 * 
	 * @param uuid UUID of player
	 * @return Whether the player is registered
	 */
	public boolean isPlayerRegistered(UUID uuid) {
		return registeredPlayers.contains(uuid);
	}

	/**
	 * Updates the hourly, daily, and weekly kills variables for the player
	 * on their scoreboard. If `updateMoney` is set to true, their money will
	 * be updated via a BukkitRunnable task (as this operation is expensive).
	 * 
	 * @param player Player whose scoreboard will be updated
	 * @param updateMoney Whether to retrieve & update the player's money too
	 */
	public void updatePlayer(Player player, boolean updateMoney) {
		setScore(player, KillsBoardVariable.HOURLY_KILLS, plugin.getTracker().getKills(player.getUniqueId(), EventType.HOURLY));
		setScore(player, KillsBoardVariable.DAILY_KILLS, plugin.getTracker().getKills(player.getUniqueId(), EventType.DAILY));
		setScore(player, KillsBoardVariable.WEEKLY_KILLS, plugin.getTracker().getKills(player.getUniqueId(), EventType.WEEKLY));
		if (!updatingMoney.contains(player.getUniqueId()) && updateMoney) {
			
			final UUID uuid = player.getUniqueId();
			updatingMoney.add(uuid);
			
			// Update their money on next tick
			new BukkitRunnable() {
				public void run() {
					updateTask.updatePlayer(uuid, KillsBoardManager.this);
					updatingMoney.remove(uuid);
				}
			}.runTask(plugin);
		}
	}
	
	/**
	 * Sets a score (i.e. variable) for a player on their scoreboard
	 * 
	 * @param player Player to set score for
	 * @param variable Variable for which to adjust score
	 * @param newScore New score
	 */
	public void setScore(Player player, KillsBoardVariable variable, int newScore) {
		
		if (!registeredPlayers.contains(player.getUniqueId()))
			return;
		
		// Send packet to player individually to update the score
		// (If their score was updated using Bukkit's APIs, the scores would
		// also be updated for everyone else.)
		WrapperPlayServerScoreboardScore setScore = new WrapperPlayServerScoreboardScore();
		setScore.setObjectiveName(OBJECTIVE_NAME);
		setScore.setScoreboardAction(ScoreboardAction.CHANGE);
		setScore.setScoreName(variable.getDisplayName(plugin.getConfiguration()));
		setScore.setValue(newScore);
		setScore.sendPacket(player);
		
	}
	
	public static enum KillsBoardVariable {
		
		HOURLY_KILLS((config) -> config.scoreboard.objHourly),
		DAILY_KILLS((config) -> config.scoreboard.objDaily),
		WEEKLY_KILLS((config) -> config.scoreboard.objWeekly),
		TOKENS((config) -> config.scoreboard.objTokens);
		
		private Function<KillEventsConfig, String> getDisplayNameFunction;
		
		KillsBoardVariable(Function<KillEventsConfig, String> getDisplayNameFunction) {
			this.getDisplayNameFunction = getDisplayNameFunction;
		}
		
		public String getDisplayName(KillEventsConfig config) {
			return getDisplayNameFunction.apply(config);
		}
		
	}
	
}
