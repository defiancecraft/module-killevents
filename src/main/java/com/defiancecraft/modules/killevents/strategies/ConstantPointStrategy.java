package com.defiancecraft.modules.killevents.strategies;

import org.bukkit.entity.Player;

/**
 * A crude implementation of a point strategy that uses a
 * predetermined number as the number of points.
 */
public class ConstantPointStrategy implements PointStrategy {

	private int amount;
	
	public ConstantPointStrategy(int amount) {
		this.amount = amount;
	}

	@Override
	public int calculatePoints(Player killer, Player victim) {
		return amount;
	}
	
}
