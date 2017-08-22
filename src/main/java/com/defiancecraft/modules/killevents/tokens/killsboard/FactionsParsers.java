package com.defiancecraft.modules.killevents.tokens.killsboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.defiancecraft.modules.killevents.tokens.TokenParserFunction;
import com.massivecraft.factions.entity.MPlayer;

public class FactionsParsers {

	private boolean enabled = false;
	
	public FactionsParsers() {
		enabled = Bukkit.getPluginManager().isPluginEnabled("Factions");
	}
	
	@TokenParserFunction
	public String parseFactionPower(String input, Player player) {
		// Only run if enabled and contains string
		if (enabled && (input.contains("{fpower}") || input.contains("{fmaxpower}"))) {
			MPlayer mPlayer = MPlayer.get(player);
			input = input.replace("{fpower}", Integer.toString(mPlayer.getPowerRounded()))
					     .replace("{fmaxpower}", Integer.toString(mPlayer.getPowerMaxRounded()));
		}
		
		return input;
	}
	
}
