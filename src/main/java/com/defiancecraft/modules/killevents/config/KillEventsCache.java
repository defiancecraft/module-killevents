package com.defiancecraft.modules.killevents.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.defiancecraft.modules.killevents.config.components.SerialCountdownSign;
import com.defiancecraft.modules.killevents.config.components.SerialSignWall;
import com.defiancecraft.modules.killevents.util.EventType;

public class KillEventsCache {

	// Defined countdown signs
	public List<SerialCountdownSign> countdownSigns = new ArrayList<>();
	
	// Defined leaderboards (sign walls)
	public List<SerialSignWall> leaderboardSignWalls = new ArrayList<>();

	// Timers for events
	public int hourlyElapsed = 0;
	public int dailyHours = 0;
	public int weeklyHours = 0;
	
	// List of users that are pending reward (concurrent implementation as
	// this map may be accessed by multiple threads - command processing,
	// event ending, etc.)
	public ConcurrentHashMap<UUID, List<PendingRewardRecord>> pendingReward = new ConcurrentHashMap<>();
	
	public static class PendingRewardRecord {
		
		public EventType type;
		public int kills;
		public int place;
		
		public PendingRewardRecord(EventType type, int kills, int place) {
			this.type = type;
			this.kills = kills;
			this.place = place;
		}
		
	}
	
}
