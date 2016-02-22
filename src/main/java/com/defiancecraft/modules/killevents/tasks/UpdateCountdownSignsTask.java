package com.defiancecraft.modules.killevents.tasks;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.KillEventsConfig;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.config.components.SerialCountdownSign;
import com.defiancecraft.modules.killevents.util.EventType;

// TODO: refactor so values are saved across event types and reused
public class UpdateCountdownSignsTask extends BukkitRunnable {

	private static final String SUBST_TIME = "{time}";
	private KillEvents plugin;
	
	public UpdateCountdownSignsTask(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		for (SerialCountdownSign sign : new ArrayList<SerialCountdownSign>(plugin.getConfiguration().cache.countdownSigns)) {
			
			Block signBlock = sign.location.toBlock();
			
			// Ensure block is a sign
			if (signBlock.getState() == null || !(signBlock.getState() instanceof Sign))
				continue;
			
			updateSign((Sign)signBlock.getState(), sign.type);
			
		}
	}
	
	private void updateSign(Sign sign, EventType type) {
		
		KillEventsConfig config = plugin.getConfiguration();
		EventConfig eventConfig = plugin.getConfiguration().getEventConfig(type);
		int remainingTicks = 0;
		int hourlyElapsed = plugin.getEventEndTask().getCurrentHourlyElapsed();
		
		// Calculate remaining ticks until event ends
		switch (type) {
		case HOURLY: remainingTicks = EventType.HOURLY.getTicks() - hourlyElapsed; break;
		case DAILY:  remainingTicks = EventType.DAILY.getTicks() - config.cache.dailyHours * 60 * 60 * 20 - hourlyElapsed; break;
		case WEEKLY: remainingTicks = EventType.WEEKLY.getTicks() - config.cache.weeklyHours * 60 * 60 * 20 - hourlyElapsed; break;
		default: break;
		}
		
		// Convert to nice date format
		int remainingSeconds = remainingTicks / 20;
		StringBuilder timeBuilder = new StringBuilder();
		
		// Days
		if (remainingSeconds >= 60 * 60 * 24) {
			timeBuilder.append(remainingSeconds / (60 * 60 * 24))
					   .append("d");
			remainingSeconds %= (60 * 60 * 24);
		}
		
		// Hours
		if (remainingSeconds >= 60 * 60) {
			timeBuilder.append(remainingSeconds / (60 * 60))
					   .append("h");
			remainingSeconds %= (60 * 60);
		}
		
		// Minutes
		if (remainingSeconds >= 60) {
			timeBuilder.append(remainingSeconds / (60))
					   .append("m");
			remainingSeconds %= (60);
		}
		
		// Seconds
		timeBuilder.append(remainingSeconds)
				   .append("s");
		
		String time = timeBuilder.toString();
		boolean changed = false;
		
		// Process each line and change it
		for (int i = 0; i < Math.min(eventConfig.countdownSignFormat.size(), 4); i++) {
			// Replace {time} substitute in sign format, and format colour codes
			String line = ChatColor.translateAlternateColorCodes(
				'&',
				eventConfig.countdownSignFormat.get(i)
					.replace(SUBST_TIME, time)
			);
			
			// Check if anything changed
			if (!sign.getLine(i).equals(line)) {
				sign.setLine(i, line);
				changed = true;
			}
		}
		
		if (changed)
			sign.update();
		
	}
	
}
