package com.defiancecraft.modules.killevents.tasks;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.config.components.SerialCountdownSign;
import com.defiancecraft.modules.killevents.util.EventType;

public class UpdateCountdownSignsTask extends BukkitRunnable {

	private static final String SUBST_TIME = "{time}";
	private KillEvents plugin;
	
	public UpdateCountdownSignsTask(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		// Update each registered countdown sign
		for (SerialCountdownSign sign : new ArrayList<SerialCountdownSign>(plugin.getConfiguration().cache.countdownSigns)) {
			
			Block signBlock = sign.location.toBlock();
			
			// Ensure block is a sign
			if (signBlock.getState() == null || !(signBlock.getState() instanceof Sign))
				continue;
			
			updateSign((Sign)signBlock.getState(), sign.type);
			
		}
	}
	
	private void updateSign(Sign sign, EventType type) {
		
		EventConfig eventConfig = plugin.getConfiguration().getEventConfig(type);
		
		String time = plugin.getEventEndTask().getRemainingTimeString(type, "{days|d} {hours|h} {minutes|m} {seconds|s}"); 
		boolean changed = false; // Whether the sign needs to be updated
		
		// Process each line from the format configuration and
		// update sign accordingly
		for (int i = 0; i < Math.min(eventConfig.countdownSignFormat.size(), 4); i++) {
			// Replace {time} substitute in sign format, and format colour codes
			String line = ChatColor.translateAlternateColorCodes(
				'&',
				eventConfig.countdownSignFormat.get(i).replace(SUBST_TIME, time)
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
