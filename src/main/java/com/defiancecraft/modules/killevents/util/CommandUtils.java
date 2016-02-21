package com.defiancecraft.modules.killevents.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class CommandUtils {

	private static final String SUBST_PLAYER = "{player}";
	private static final String SUBST_KILLS = "{kills}";
	
	/**
	 * Formats a command, substituting in values for the player's name
	 * and number of kills. 
	 * <p>
	 * This method will attempt to resolve the player's name
	 * from their {@link OfflinePlayer} record. If they have never logged
	 * in, this function will likely return an empty string ("").
	 * 
	 * @param command Command to format
	 * @param uuid UUID of player
	 * @param kills Number of kills
	 * @return Formatted command
	 */
	public static String formatCommand(String command, UUID uuid, int kills) {
		
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		if (offlinePlayer == null)
			return "";
		
		return formatCommand(command, offlinePlayer.getName(), kills);
		
	}
	
	/**
	 * Formats a command, substituting in values for the player's name
	 * and number of kills.
	 * 
	 * @param command Command to format
	 * @param playerName Name of player
	 * @param kills Number of kills
	 * @return Formatted command
	 */
	public static String formatCommand(String command, String playerName, int kills) {
		return command
				.replace(SUBST_PLAYER, playerName)
				.replace(SUBST_KILLS, Integer.toString(kills));
	}
	
}
