package com.defiancecraft.modules.killevents.strategies;

import org.bukkit.entity.Player;

/**
 * A strategy for calculating a number of 'points' that a
 * player is given for getting a kill. This takes information
 * from the kill itself, i.e. the victim and the killer at the time.
 * <p>
 * How the number of points is determined is dependent upon
 * implementation. A PointStrategy simply provides an interface to
 * input the values for these calculations to arrive at a number
 * of points.
 */
public interface PointStrategy {

	public int calculatePoints(Player killer, Player victim);
	
}
