package com.defiancecraft.modules.killevents.config.components;

import java.util.Arrays;
import java.util.List;

public class ScoreboardConfig {
	
	// Scoreboard title
	public String title = "DefianceCraft";

	// Lines (in order)
	public List<String> lines = Arrays.asList(
		"{tokens}",
		"{hpoints} ({hplace})",
		"{dpoints} ({dplace})",
		"{wpoints} ({wplace})",
		"{online}",
		"{max}",
		"{htime}",
		"{dtime}",
		"{wtime}",
		"{fpower}",
		"{fmaxpower}",
		"{restock}",
		"{tokenreward}",
		"{votes24h}"
	);

	// How often the scoreboard should update in ticks
	public long frequencyTicks = 6000;
	
	// How often the economy cache should be updated
	public long economyTicks = 100;
	
	// Whether scoreboard enforcement is passive (i.e.
	// won't be enforced if objective already exists in slot)
	public boolean passive = false;
	
}