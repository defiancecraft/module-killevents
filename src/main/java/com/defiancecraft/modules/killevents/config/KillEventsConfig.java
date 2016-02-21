package com.defiancecraft.modules.killevents.config;

import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.config.components.ScoreboardConfig;
import com.defiancecraft.modules.killevents.config.components.SerialRegion;
import com.defiancecraft.modules.killevents.util.EventType;
import com.google.gson.annotations.SerializedName;

public class KillEventsConfig {

	// Scoreboard config
	public ScoreboardConfig scoreboard = new ScoreboardConfig();
	
	// Event configs
	public EventConfig hourlyEvent = new EventConfig();
	public EventConfig dailyEvent = new EventConfig();
	public EventConfig weeklyEvent = new EventConfig();
	
	// Leaderboard signs
	public String signNameFormat = "&b{name}";
	public String signKillsFormat = "&c{kills}";
	public int leaderboardUpdateFrequencyTicks = 600;
	
	// PvP region
	public SerialRegion pvpRegion = null; 
	
	// Cache (storage)
	@SerializedName("__cache")
	public KillEventsCache cache = new KillEventsCache();
	
	public EventConfig getEventConfig(EventType type) {
		switch (type) {
		case HOURLY: return hourlyEvent;
		case DAILY: return dailyEvent;
		case WEEKLY: return weeklyEvent;
		default: return null;
		}
	}
	
}
