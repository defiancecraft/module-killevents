package com.defiancecraft.modules.killevents.config.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventConfig {
	
	// Default list of messages to be broadcasted for an event (demonstrating usage of config)
	private static final HashMap<Integer, String> DEFAULT_MESSAGES;
	private static final LinkedHashMap<Integer, List<String>> DEFAULT_PLAYER_COMMANDS;
	
	// List of commands to be executed on the player (when they connect if not online) 
	public LinkedHashMap<Integer, List<String>> playerCommands = DEFAULT_PLAYER_COMMANDS;
	
	// List of commands to be executed when the event ends
	public LinkedHashMap<Integer, List<String>> serverCommands = new LinkedHashMap<>();
	
	// Messages to be broadcasted at various points throughout the event
	public Map<Integer, String> messages = DEFAULT_MESSAGES;
	
	// Sign display
	public List<String> countdownSignFormat = Arrays.asList(
		"&1&lThis event",
		"Time left:",
		"{time}",
		""
	);	
	
	static {
		DEFAULT_MESSAGES = new HashMap<>();
		DEFAULT_MESSAGES.put(30 * 60, "30 minutes since the event started");
		DEFAULT_PLAYER_COMMANDS = new LinkedHashMap<>();
		DEFAULT_PLAYER_COMMANDS.put(1, Arrays.asList("/tell {player} You came 1st with {kills} kills!"));
		DEFAULT_PLAYER_COMMANDS.put(2, Arrays.asList("/tell {player} You came 2nd, lmao."));
	}
	
}
