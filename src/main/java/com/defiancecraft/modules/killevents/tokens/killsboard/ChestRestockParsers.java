package com.defiancecraft.modules.killevents.tokens.killsboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.defiancecraft.modules.chestrestock.tasks.RestockTask;
import com.defiancecraft.modules.killevents.tokens.TokenParserFunction;

public class ChestRestockParsers {

	private boolean enabled = false;
	
	public ChestRestockParsers() {
		enabled = Bukkit.getPluginManager().isPluginEnabled("ChestRestock");
	}
	
	@TokenParserFunction
	public String parseRestockTime(String input, Player player) {
		if (!enabled || input.contains("{restock}"))
			return input;
		
		input.replace("{restock}", RestockTask.getNextRestock().)
	}
	
}
