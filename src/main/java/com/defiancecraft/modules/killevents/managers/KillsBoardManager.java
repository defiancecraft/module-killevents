package com.defiancecraft.modules.killevents.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;
import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.components.ScoreboardConfig;
import com.defiancecraft.modules.killevents.tasks.UpdateKillsBoardTask;
import com.defiancecraft.modules.killevents.tokens.TokenParser;

public class KillsBoardManager  {

	private static final String OBJECTIVE_NAME = "KillEvents";
	
	// Value for field 'Position' for sidebar position of packet 0x3D (display scoreboard) 
	private static final int DISPLAY_POSITION_SIDEBAR = 1;
	
	// Cache of scoreboards for players, so that unnecessary score update
	// packets are not sent if nothing changes.
	private final Map<UUID, List<String>> scoreboardCache = new HashMap<>();
	
	// List of registered parsers
	private final Set<TokenParser> parsers = new HashSet<>();
	
	// Reference to plugin
	private KillEvents plugin;
	
	// Update board task
	private UpdateKillsBoardTask updateTask;
	
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
		
		// Update the scores of player
		this.updateScores(player);
		
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
		
		// TODO: test this -- won't it clear the board for everyone?
		
		if (board != null && (objective = board.getObjective(OBJECTIVE_NAME)) != null)
			objective.unregister();
	
		this.registeredPlayers.remove(player.getUniqueId());
		this.scoreboardCache.remove(player.getUniqueId());
	}
	
	/**
	 * Unregisters a player and, if they are online, unregisters the objective
	 * for the player (and so hides the scoreboard).
	 * 
	 * @param uuid UUID of player
	 */
	public void unregisterPlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null && player.isOnline()) {
			unregisterPlayer(player);
		} else {
			this.registeredPlayers.remove(uuid);
			this.scoreboardCache.remove(uuid);
		}
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
	 * Registers a parser that will be called when processing the
	 * configured lines for the scoreboard (see {@link ScoreboardConfig#lines}).
	 * 
	 * @param parser Parser to register
	 * @return Whether the parser was added (it will not be if present)
	 */
	public boolean registerParser(TokenParser parser) {
		return this.parsers.add(parser);
	}
	
	/**
	 * Removes a parser from the chain of parsers for scoreboard
	 * 'lines' (see {@link ScoreboardConfig#lines}). After removing,
	 * subsequent calls to {@link #updateScores(Player)} will not
	 * call the parser.
	 * 
	 * @param parser Parser to remove
	 * @return Whether parser was removed
	 */
	public boolean unregisterParser(TokenParser parser) {
		return this.parsers.remove(parser);
	}
	
	/**
	 * Parses a string's tokens by running it through the chain
	 * of registered parsers.
	 *  
	 * @param input Input to parse
	 * @param player Player to pass to parsers
	 * @return Parsed string
	 */
	private String parseString(String input, Player player) {
		
		// Attempt to execute each parser
		for (TokenParser parser : parsers) {
			try {
				if (parser != null)
					input = parser.apply(input, player);
			} catch (Exception e) {
				// Log any exception
				plugin.getLogger().severe(
					String.format(
						"Failed to execute parser (class: %s).\nException type: %s\nException message: %s",
						parser.getClass().getName(),
						e.getClass().getName(),
						e.getMessage()
					)
				);
				
				continue;
			}
		}
		return input;
	}
	
	public void updateScores(Player player) {
		
		// Synchronize on player's entry in cache to prevent race conditions;
		// This method can be called from a periodic task, or from event handlers.
		synchronized(scoreboardCache.containsKey(player.getUniqueId()) ? scoreboardCache.get(player.getUniqueId()) : new Object()) {
			
			List<String> lines = plugin.getConfiguration().scoreboard.lines;
			List<String> newLines = new ArrayList<>();
			UUID uuid = player.getUniqueId();
			
			// Process all lines first, so there is no delay between
			// updating scores on the scoreboard. E.g. if one line
			// included a DB query, the next would take longer to
			// appear.
			for (String line : lines)
				newLines.add(parseString(line, player));
	
			// Update scores line by line
			for (int i = 0; i < lines.size(); i++) {
				
				String line = newLines.get(i);
				
				// Update score (send packet) if:
				//   - player is not in cache, or
				//   - current line in cache does not match this
				//     processed line (i.e. it has changed) 
				if (!scoreboardCache.containsKey(uuid)
						|| !scoreboardCache.get(uuid).get(i).equals(line)) {
					removeScore(player, scoreboardCache.get(uuid).get(i)); // Remove old score
					setScore(player, line, lines.size() - i);  // Add new score with value inversely high compared to index (scoreboard is sorted descending)
				}
			}
			
			// Update cache with new lines
			scoreboardCache.put(uuid, newLines);
			
		}
		
	}
	
	/**
	 * Removes a score from a player's scoreboard by constructing
	 * and sending them a packet. Note that the packet is sent only to
	 * the player so the score does not change for the entire server.
	 * <p>
	 * If the length of `name` exceeds 16 characters, it will be truncated.
	 * 
	 * @param player Player to remove score from
	 * @param name Name of score (must be exact; max length 16 characters)
	 */
	private void removeScore(Player player, String name) {
		name = name.length() > 16 ? name.substring(0, 16) : name;
		
		WrapperPlayServerScoreboardScore removeScore = new WrapperPlayServerScoreboardScore();
		removeScore.setObjectiveName(OBJECTIVE_NAME);
		removeScore.setScoreboardAction(ScoreboardAction.REMOVE);
		removeScore.setScoreName(name);
		removeScore.sendPacket(player);
	}
	
	/**
	 * Sets a score in a player's scoreboard by constructing and
	 * sending them a packet. Note that the packet is sent only to
	 * the player so the score does not change for the entire server.
	 * <p>
	 * If the length of `name` exceeds 16 characters, it will be truncated.
	 * 
	 * @param player Player to set score for
	 * @param name Name of score (must be exact; max length 16 characters)
	 * @param score Score value
	 */
	private void setScore(Player player, String name, int score) {
		name = name.length() > 16 ? name.substring(0, 16) : name;
		
		WrapperPlayServerScoreboardScore setScore = new WrapperPlayServerScoreboardScore();
		setScore.setObjectiveName(OBJECTIVE_NAME);
		setScore.setScoreboardAction(ScoreboardAction.CHANGE);
		setScore.setScoreName(name);
		setScore.setValue(score);
		setScore.sendPacket(player);
	}
	
}
