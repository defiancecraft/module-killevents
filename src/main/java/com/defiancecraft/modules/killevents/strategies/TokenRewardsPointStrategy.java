package com.defiancecraft.modules.killevents.strategies;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.defiancecraft.modules.tokenrewards.TokenRewards;
import com.defiancecraft.modules.tokenrewards.rewarding.TokenRewarder;

public class TokenRewardsPointStrategy implements PointStrategy {

	public TokenRewardsPointStrategy() {
		
		TokenRewards plugin;
		if ((plugin = JavaPlugin.getPlugin(TokenRewards.class)) == null || !plugin.isEnabled())
			throw new IllegalStateException("TokenRewards plugin is not present/disabled.");
	}
	
	@Override
	public int calculatePoints(Player killer, Player victim) {
		return TokenRewarder.getTokenReward(victim, TokenRewards.getConfiguration());
	}
	
}
