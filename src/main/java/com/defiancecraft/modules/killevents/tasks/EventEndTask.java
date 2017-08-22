package com.defiancecraft.modules.killevents.tasks;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.KillEventsCache;
import com.defiancecraft.modules.killevents.config.KillEventsCache.PendingRewardRecord;
import com.defiancecraft.modules.killevents.config.KillEventsConfig;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.util.CommandUtils;
import com.defiancecraft.modules.killevents.util.EventType;
import com.defiancecraft.modules.killevents.util.PlayerRewarding;

public class EventEndTask extends BukkitRunnable {

	protected KillEvents plugin;
	protected long lastTimeMillis;
	protected boolean hasExecuted = false;
	
	public EventEndTask(KillEvents plugin) {
		this.plugin = plugin;
		this.lastTimeMillis = System.currentTimeMillis();
	}
	
	public void run() {

		this.hasExecuted = true;
		this.lastTimeMillis = System.currentTimeMillis();
		
		// Run hourly event (this task is hourly)
		endEvent(EventType.HOURLY);
		
		// Increment daily hours and run if 24
		if (++plugin.getConfiguration().cache.dailyHours >= 24)
			endEvent(EventType.DAILY);
		
		// Increment weekly hours and run if 168 (24 * 7)
		if (++plugin.getConfiguration().cache.weeklyHours >= 24 * 7)
			endEvent(EventType.WEEKLY);
		
	}
	
	public int getCurrentHourlyElapsed() {
		
		// TODO: move this to separate manager?
		
		// Calculate difference in ticks from last run of hourly event to now
		long difference = System.currentTimeMillis() - lastTimeMillis;
		int elapsed = (int)difference / 50;
		
		// If we haven't executed yet, add on the previous hourly elapsed
		if (!hasExecuted)
			elapsed += plugin.getConfiguration().cache.hourlyElapsed;
		
		return elapsed;
		
	}
	
	/**
	 * Gets the remaining time for an event
	 * 
	 * @param event Event to get remaining time for 
	 * @return Duration representing remaining time until event ends
	 */
	public Duration getRemainingTime(EventType event) {
		
		int hourlyElapsed = getCurrentHourlyElapsed();
		KillEventsConfig config = plugin.getConfiguration();
		
		int ticks = -1;
		
		// Calculate remaining ticks until event ends
		switch (event) {
		case HOURLY: ticks = EventType.HOURLY.getTicks() - hourlyElapsed; break;
		case DAILY:  ticks = EventType.DAILY.getTicks() - config.cache.dailyHours * 60 * 60 * 20 - hourlyElapsed; break;
		case WEEKLY: ticks = EventType.WEEKLY.getTicks() - config.cache.weeklyHours * 60 * 60 * 20 - hourlyElapsed; break;
		}

		if (ticks == -1)
			return null;
		
		return Duration.ofMillis(ticks * 50l);
		
	}
	
	/**
	 * Gets the remaining time for an event as a string formatted with the
	 * remaining days, hours, minutes, and seconds. Note that fields with values
	 * of 0 will not be displayed unless they have a temporal unit of higher value
	 * with a non-zero value.
	 * <p>
	 * E.g. for a format string <code>{days|d} {hours|h} {minutes|m} {seconds|s}</code>,
	 *      if there are 0 days, 4 hours, 0 minutes, and 5 seconds, the following
	 *      string would be displayed:
	 *      <code>4h 0m 5s</code>
	 * <p>
	 * The format string is a plain string containing groups that will be replaced
	 * of the following form:
	 * <pre>
     * &lt;group&gt;     ::= "{" &lt;unit&gt; &lt;opt-extra&gt; "}"
     * &lt;unit&gt;      ::= "days" | "hours" | "minutes" | "seconds"
     * &lt;opt-extra&gt; ::= "" | "|" &lt;extra&gt; 
	 * </pre>
	 * where <code>&lt;extra&gt;</code> is content that will only be displayed alongside
	 * the field if its value is greater than 0 or there is a higher temporal unit with
	 * a non-zero value (see example above).
	 * 
	 * @param event Event to get remaining time for
	 * @param format Format as per {@link MessageFormat#format(String, Object...)}
	 * @return Formatted string
	 */
	public String getRemainingTimeString(EventType event, String format) {
		
		// Get remaining time in seconds for event
		long seconds = getRemainingTime(event).getSeconds();
		
		// Convert seconds to days, hours, minutes, and seconds
		long remDays = seconds / (60 * 60 * 24);
		long remHours = (seconds % (60 * 60 * 24)) / (60 * 60);
		long remMinutes = (seconds % (60 * 60)) / (60);
		long remSeconds = seconds % 60;
		
		// Regex is (unescaped): \{UNIT(?:\|([^\}]+))?\}
		// \{		matches opening curly brace
		// UNIT		the temporal unit (days, hours, minutes, or seconds)
		// (?:\ 	new uncaptured group; can be used to make optional without capturing
		//     |		matches pipe character
		// 	   ([^\}]+) capturing group of 1+ non-closing curly brace characters
		// )?		closes the uncaptured group and makes it optional
		// \}		closing curly brace
		return format.replaceAll("\\{days(?:\\|([^\\}]+))?\\}", seconds > 24 * 60 * 60 ? remDays + "$1" : "")
				     .replaceAll("\\{hours(?:\\|([^\\}]+))?\\}", seconds > 60 * 60 ? remHours + "$1" : "")
				     .replaceAll("\\{minutes(?:\\|([^\\}]+))?\\}", seconds > 60 ? remMinutes + "$1" : "")
				     .replaceAll("\\{seconds(?:\\|([^\\}]+))?\\}", remSeconds + "$1");
		
	}
	
	public void shutdown() {
		
		int hourlyElapsed = getCurrentHourlyElapsed();
		
		if (hourlyElapsed >= EventType.HOURLY.getTicks()) {
			Bukkit.getLogger().warning("Timing inconsistency detected! Hourly check took longer than expected. Future timing may not be aligned to current event timing.");
			hourlyElapsed %= EventType.HOURLY.getTicks();
		}
		
		plugin.getConfiguration().cache.hourlyElapsed = hourlyElapsed;
		
	}
	
	protected void endEvent(EventType type) {
		
		// Get the appropriate event config
		EventConfig eventConfig = plugin.getConfiguration().getEventConfig(type);
		
		// Ensure event is configured
		if (eventConfig == null) {
			plugin.getLogger().severe(String.format("Event %s is not configured; please create a configuration for the event. Plugin will be disabled.", type.name()));
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		
		// Get players that placed
		List<Entry<UUID, Integer>> placements = plugin.getTracker().getEventKills(type)
			.entrySet()
			.stream()
			.sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sort descending
			.collect(Collectors.toList()); // Collect as ordered list
		
		// Reset event kills (we already have the kills stored)
		plugin.getTracker().resetEventKills(type);
		
		// Sequentially iterate over each place
		for (int place = 0; place < placements.size(); place++) {
			
			Entry<UUID, Integer> entry = placements.get(place);
			
			int canonicalPlace = place + 1; 	   // Player's place in canonical format (e.g. 1 for 1st instead of 0)
			int kills          = entry.getValue(); // Player's achieved number of kills in this event
			UUID uuid          = entry.getKey();   // UUID of player
			
			// Execute serverCommands (if any)
			for (String command : eventConfig.serverCommands.getOrDefault(canonicalPlace, Collections.<String>emptyList())) {
				
				try {
					Bukkit.dispatchCommand(
						Bukkit.getConsoleSender(),
						CommandUtils.formatCommand(command, uuid, kills));
				} catch (CommandException e) {
					Bukkit.getLogger().warning("[KillEvents] An error occurred executing serverCommand '" + command + "' for player UUID '" + uuid + "'; stack trace below.");
					e.printStackTrace();
				}
				
			}
			
			// Attempt to reward player (execute playerCommands) if they are online;
			// otherwise, add them to the pending reward list in the config cache.
			if (!PlayerRewarding.rewardOnlinePlayer(
					entry.getKey(),
					eventConfig.playerCommands.getOrDefault(canonicalPlace, Collections.<String>emptyList()),
					kills)) {
				
				// Wrap in ArrayList in case of immutable implementation
				List<PendingRewardRecord> records = new ArrayList<>(
						plugin.getConfiguration().cache.pendingReward.getOrDefault(
							uuid,
							Collections.<PendingRewardRecord>emptyList()
						)
				);
				
				records.add(new KillEventsCache.PendingRewardRecord(type, kills, canonicalPlace));
				plugin.getConfiguration().cache.pendingReward.put(uuid, records);
				
			}
			
		}
		
		// Yeah, fuck it, we might as well update all the leaderboard sign walls
		plugin.getLeaderboardSignManager().updateSigns();
		
		// Reset timer for the event
		switch (type) {
		case HOURLY:
			plugin.getConfiguration().cache.hourlyElapsed = 0;
			break;
		case DAILY:
			plugin.getConfiguration().cache.dailyHours = 0;
			break;
		case WEEKLY:
			plugin.getConfiguration().cache.weeklyHours = 0;
			break;
		}
		
	}
	
}
