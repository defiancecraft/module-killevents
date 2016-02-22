package com.defiancecraft.modules.killevents.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.components.SerialCountdownSign;
import com.defiancecraft.modules.killevents.util.EventType;

public class BlockListener implements Listener {

	private static final String SIGN_COUNTDOWN_TRIGGER = "[KECD]";
	private KillEvents plugin;
	
	public BlockListener(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onSignChanged(SignChangeEvent e) {
		
		// Process countdown signs
		if (e.getLine(0).equalsIgnoreCase(SIGN_COUNTDOWN_TRIGGER)) {
			
			SerialCountdownSign cdSign = null;
			if (e.getLine(1).equalsIgnoreCase("hourly"))
				cdSign = new SerialCountdownSign(e.getBlock().getLocation(), EventType.HOURLY);
			else if (e.getLine(1).equalsIgnoreCase("daily"))
				cdSign = new SerialCountdownSign(e.getBlock().getLocation(), EventType.DAILY);
			else if (e.getLine(1).equalsIgnoreCase("weekly"))
				cdSign = new SerialCountdownSign(e.getBlock().getLocation(), EventType.WEEKLY);
			
			if (cdSign != null) {
				plugin.getConfiguration().cache.countdownSigns.add(cdSign);
				e.getPlayer().sendMessage(ChatColor.GREEN + "Countdown sign for event '" + cdSign.type.name() + "' created.");
			}

		}
			
	}
	
}
