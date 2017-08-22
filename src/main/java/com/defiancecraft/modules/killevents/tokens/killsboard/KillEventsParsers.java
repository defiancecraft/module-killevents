package com.defiancecraft.modules.killevents.tokens.killsboard;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.tasks.EventEndTask;
import com.defiancecraft.modules.killevents.tokens.TokenParserFunction;
import com.defiancecraft.modules.killevents.util.EventType;
import com.defiancecraft.modules.killevents.util.IntUtils;

public class KillEventsParsers {

	private KillEvents plugin;
	
	public KillEventsParsers(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Parses hourly, daily, and weekly point/place substitutes, i.e.:
	 * <code>{hpoints}, {dpoints}, {wpoints}, {hplace}, {dplace}, {wplace}</code>
	 * 
	 * @param input Input string 
	 * @param player Player to parse for
	 * @return Parsed string
	 */
	@TokenParserFunction
	public String parsePoints(String input, Player player) {
		if (!input.matches("[hdw]p(oints|lace)"))
			return input;
		
		for (EventType event : EventType.values())
			input = parsePointsForEvent(input, player, event);
		
		return input;
	}
	
	/**
	 * Parses the time remaining substitutes to respective time strings (e.g. 5d4h3m):
	 * <code>{wtime}, {dtime}, {htime}</code>
	 * 
	 * @param input Input string
	 * @param player Player to parse for (can be null)
	 * @see EventEndTask#getRemainingTimeString(EventType)
	 * @return Formatted input string
	 */
	@TokenParserFunction
	public String parseTimeRemaining(String input, Player player) {
		// Replace each substitute with an appropriate 'time string' (e.g. 5d4h3m2s)
		// Check for occurrence of each substitute to avoid unnecessary 'time string' computation
		if (input.contains("wtime"))
			input = input.replace("wtime", plugin.getEventEndTask().getRemainingTimeString(EventType.WEEKLY, "{days|d} {hours|h} {minutes|m}"));
		if (input.contains("dtime"))
			input = input.replace("dtime", plugin.getEventEndTask().getRemainingTimeString(EventType.DAILY, "{days|d} {hours|h} {minutes|m}"));
		if (input.contains("htime"))
			input = input.replace("htime", plugin.getEventEndTask().getRemainingTimeString(EventType.HOURLY, "{days|d} {hours|h} {minutes|m}"));
		
		return input;
	}
	
	/**
	 * Helper function to parse points and place substitutes for an event
	 * 
	 * @param input Input string
	 * @param player Player to fetch points/place for
	 * @param event Event to check
	 * @return String with substitute strings correctly substituted (e.g. {hpoints} -> 1)
	 */
	private String parsePointsForEvent(String input, Player player, EventType event) {
		
		// Get prefix of event (should be 'h', 'd', or 'w') and consequent
		// place and point substitute strings
		char prefix = event.name().toLowerCase().charAt(0);
		String pointString = "{" + prefix + "points}";
		String placeString = "{" + prefix + "place}";
		
		// Fetch kills to obtain kills or place
		Map<UUID, Integer> kills = plugin.getTracker().getEventKills(event);
		
		// Replace points substitute strings
		if (input.contains(pointString))
			input = input.replace(pointString, kills.get(player.getUniqueId()).toString());
		
		// Replace place substitute strings
		if (input.contains(placeString))
			input = input.replace(placeString, getPlace(kills, player.getUniqueId()));
		
		return input;
	}
	
	/**
	 * Gets the place of a user in a tracked event (i.e. a map of UUIDs to
	 * kills/points).
	 * 
	 * @param kills Map of UUIDs to kills/points for the event
	 * @param uuid UUID of player to get place of
	 * @return Place of player and suffix (e.g. "1st") if found, otherwise "".
	 */
	private String getPlace(Map<UUID, Integer> kills, UUID uuid) {
		
		// Sort the kills by number of kills
		List<Entry<UUID, Integer>> entries = kills.entrySet()
			.stream()
			.sorted((a, b) -> IntUtils.compareReversed(a.getValue(), b.getValue())) // Descending
			.collect(Collectors.toList());

		// Find UUID within sorted list of kills
		for (int i = 0; i < entries.size(); i++) {
			if (entries.get(i).getKey().equals(uuid))
				return IntUtils.getWithSuffix(i);
		}
		
		// Return nothing if did not place
		return "";
		
	}
	
}
