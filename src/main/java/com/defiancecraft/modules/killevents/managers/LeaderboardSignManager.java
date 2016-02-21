package com.defiancecraft.modules.killevents.managers;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.config.components.SerialSignWall;
import com.defiancecraft.modules.killevents.util.EventType;
import com.sk89q.worldedit.bukkit.selections.Selection;


public class LeaderboardSignManager {

	private KillEvents plugin;
	
	public LeaderboardSignManager(KillEvents plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Updates all known sign walls
	 */
	public void updateSigns() {
		
		if (plugin.getConfiguration().cache.leaderboardSignWalls.size() < 1)
			return;
		
		Map<EventType, List<Entry<String, Integer>>> cachedSignContent = new HashMap<>();
		
		//  Process each known sign wall
		for (SerialSignWall signWall : new ArrayList<SerialSignWall>(plugin.getConfiguration().cache.leaderboardSignWalls)) {
			
			// Retrieve all possible sign content if haven't already; will be
			// an ordered list of usernames to kills.
			if (!cachedSignContent.containsKey(signWall.type))
				cachedSignContent.put(signWall.type, getSignContent(signWall.type));
			
			Selection sel = signWall.toSelection();
			Vector rightVector = sel.getWidth() == 2 ? new Vector(1, 0, 0) : new Vector(0, 0, 1);
			
			// Iterate over each pair of signs
			for (int i = 0; i < sel.getHeight(); i++) {
				
				// Get blocks `i` blocks down from the maximum point (max x, y, and z);
				// Right vector is specific to direction of sign wall and will get sign to the
				// right of the other
				Block leftBlock  = sel.getMaximumPoint().add(new Vector(0, i, 0)).subtract(rightVector).getBlock();
				Block rightBlock = leftBlock.getLocation().add(rightVector).getBlock();
				
				// Ensure both blocks are signs
				if (!(leftBlock.getState() instanceof Sign) || !(rightBlock.getState() instanceof Sign))
					continue;
				
				List<Entry<String, Integer>> signContent = cachedSignContent.get(signWall.type);
				Sign leftSign  = (Sign)leftBlock.getState();
				Sign rightSign = (Sign)rightBlock.getState();
				
				// Continue adding lines until we get to fourth line or 
				// the number of total lines is past the size of the leaderboard
				for (int line = 0; line < 4 && i * 4 + line < signContent.size(); line++) {
					
					// Lazily change and update the left sign (the user's name)
					String oldLeft = leftSign.getLine(line);
					leftSign.setLine(line, signContent.get(i * 4 + line).getKey());
					if (!oldLeft.equals(leftSign.getLine(line)))
						leftSign.update();
					
					// Do the same for right sign (the kills of that user)
					String oldRight = rightSign.getLine(line);
					rightSign.setLine(line, signContent.get(i * 4 + line).getValue().toString());
					if (!oldRight.equals(rightSign.getLine(line)))
						rightSign.update();
					
				} // Finish iterating over each line in the sign
				
			} // Finish iterating over each pair of signs
			
		} // Finish iterating over each sign wall
		
	}
	
	private List<Entry<String, Integer>> getSignContent(EventType type) {
		return plugin.getTracker().getEventKills(type).entrySet()
			.stream()
			.sorted((a, b) -> a.getValue().compareTo(b.getValue()))
			.<Entry<String, Integer>>map((e) -> {
				
				return new AbstractMap.SimpleEntry<String, Integer>(
					Bukkit.getOfflinePlayer(e.getKey()).getName(),
					e.getValue()
				);
				
			})
			.collect(Collectors.toList());
	}
	
}
