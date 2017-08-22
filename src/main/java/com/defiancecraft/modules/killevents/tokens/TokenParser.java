package com.defiancecraft.modules.killevents.tokens;

import java.util.function.BiFunction;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface TokenParser extends BiFunction<String, Player, String> {

	/**
	 * Parses a string that may or may not contain tokens. If
	 * the string does not contain any tokens relevant to this
	 * parser, the given string should be returned.
	 * 
	 * @param input String to parse
	 * @param player Player to use 
	 * @return String with tokens processed
	 */
	public String apply(String str, Player player);
	
}
