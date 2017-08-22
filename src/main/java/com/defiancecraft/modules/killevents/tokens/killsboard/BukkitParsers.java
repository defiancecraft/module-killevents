package com.defiancecraft.modules.killevents.tokens.killsboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.defiancecraft.modules.killevents.tokens.TokenParserFunction;

public class BukkitParsers {

	@TokenParserFunction
	public static String parsePlayersOnline(String input, Player p) {
		return input.replace("{online}", Integer.toString(Bukkit.getOnlinePlayers().size()));
	}
	
	@TokenParserFunction
	public static String parseMaxPlayers(String input, Player p) {
		return input.replace("{max}", Integer.toString(Bukkit.getMaxPlayers()));
	}
	
}
