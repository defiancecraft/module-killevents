package com.defiancecraft.modules.killevents.tracker;

import java.util.Map;
import java.util.UUID;

import com.defiancecraft.modules.killevents.util.EventType;

/**
 * An interface to track kills for players for different events. This
 * involves storing and maintaining the players' number of kills, as
 * well as providing the means to change them.
 */
public interface KillTracker {

	/**
	 * Gets a player's tracked number of kills for a given event
	 * 
	 * @param player Player to get kills of
	 * @param event Event to retrieve number of kills for
	 * @return The player's stored number of kills for the given event
	 */
	public int getKills(UUID player, EventType event);
	
	/**
	 * Sets a player's tracked number of kills for a given event
	 * 
	 * @param player Player to set kills for
	 * @param event Event type to set kills for
	 * @param kills New number of kills
	 * @return Whether the player's kills were set
	 */
	public boolean setKills(UUID player, EventType event, int kills);
	
	/**
	 * Resets a player's number of kills to 0 for an event (equivalent to
	 * {@code setKills(player, event, 0)}).
	 * 
	 * @param player Player to reset kills for
	 * @param event Event type
	 * @return Whether the player's kills were reset
	 */
	public boolean resetKills(UUID player, EventType event);

	/**
	 * Gets a list of kills for all players that were tracked in
	 * an event.
	 *  
	 * @param event Event to get kills for
	 * @return A map of players' UUIDs to their number of kills
	 */
	public Map<UUID, Integer> getEventKills(EventType event);
	
	/**
	 * Resets all players' kills for an event
	 *  
	 * @param event Event to reset all kills for
	 * @return Whether all kills were reset
	 */
	public boolean resetEventKills(EventType event);
	
	/**
	 * Saves the values for the kill tracker. Note that for some trackers,
	 * this may have no effect.
	 * @throws Exception If the tracker failed to save
	 */
	public void save() throws Exception;
	
}
