package com.defiancecraft.modules.killevents.config.components;

public class ScoreboardConfig {
	
	// Scoreboard title
	public String title = "DefianceCraft";

	// Objective display names
	public String objHourly = "Hourly";
	public String objDaily = "Daily";
	public String objWeekly = "Weekly";
	public String objTokens = "Tokens";

	// How often the scoreboard should update in ticks
	public long frequencyTicks = 6000;
	
	// Whether scoreboard enforcement is passive (i.e.
	// won't be enforced if objective already exists in slot)
	public boolean passive = false;
	
}