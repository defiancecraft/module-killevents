package com.defiancecraft.modules.killevents;

import java.io.File;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.defiancecraft.core.command.CommandRegistry;
import com.defiancecraft.core.database.collections.Collection;
import com.defiancecraft.core.modules.impl.JavaModule;
import com.defiancecraft.core.util.FileUtils;
import com.defiancecraft.modules.killevents.commands.AdminCommands;
import com.defiancecraft.modules.killevents.config.KillEventsConfig;
import com.defiancecraft.modules.killevents.config.components.EventConfig;
import com.defiancecraft.modules.killevents.listeners.BlockListener;
import com.defiancecraft.modules.killevents.listeners.PlayerListener;
import com.defiancecraft.modules.killevents.managers.KillsBoardManager;
import com.defiancecraft.modules.killevents.managers.LeaderboardSignManager;
import com.defiancecraft.modules.killevents.strategies.ConstantPointStrategy;
import com.defiancecraft.modules.killevents.strategies.PointStrategy;
import com.defiancecraft.modules.killevents.strategies.TokenRewardsPointStrategy;
import com.defiancecraft.modules.killevents.tasks.BroadcastMessageTask;
import com.defiancecraft.modules.killevents.tasks.EventEndTask;
import com.defiancecraft.modules.killevents.tasks.UpdateCountdownSignsTask;
import com.defiancecraft.modules.killevents.tasks.UpdateLeaderboardSignsTask;
import com.defiancecraft.modules.killevents.tokens.killsboard.BukkitParsers;
import com.defiancecraft.modules.killevents.tokens.killsboard.DefianceCraftParsers;
import com.defiancecraft.modules.killevents.tokens.killsboard.FactionsParsers;
import com.defiancecraft.modules.killevents.tokens.killsboard.KillEventsParsers;
import com.defiancecraft.modules.killevents.tracker.JsonKillTracker;
import com.defiancecraft.modules.killevents.tracker.KillTracker;
import com.defiancecraft.modules.killevents.util.EventType;

public class KillEvents extends JavaModule {

	// Primary config instance
	private KillEventsConfig config;
	
	// Tracker for player's kills across events
	private KillTracker tracker;
	
	// Event end checking task (handles ending of events)
	private EventEndTask eventEndTask;
	
	// 'Kills' board (main scoreboard) manager
	private KillsBoardManager killsBoardManager;
	
	// Leaderboard signs (sign walls) manager
	private LeaderboardSignManager leaderboardSignManager; 
	
	// Vault economy
	private Economy economy = null;
	
	// Point strategy
	private PointStrategy pointStrategy;
	
	// -------------
	// Overriden methods
	// -------------
	
	@Override
    public void onEnable() {
    
    	// Load config
    	this.config = getConfig(KillEventsConfig.class);

    	// Try to create TokenRewards strategy; if that fails, use constant
    	try {
    		this.pointStrategy = new TokenRewardsPointStrategy();
    	} catch (IllegalStateException e) {
    		getLogger().warning(String.format("Failed to create TokenRewards point strategy: %s.", e.getMessage()));
    		this.pointStrategy = new ConstantPointStrategy(1);
    	}
    	
    	// Create kill tracker
    	this.tracker = new JsonKillTracker(new File(FileUtils.getSharedDirectory(), "killevents.json.gz.cache"), true);
    	
    	// Setup Vault
    	if (!this.setupVault())
    		getLogger().warning("Failed to setup Vault. Please ensure the plugin is installed and an economy is setup. Plugin may fail.");

    	// Create scoreboard & parsers
    	this.killsBoardManager = new KillsBoardManager(this);
    	this.killsBoardManager.registerParser(BukkitParsers::parsePlayersOnline); // {online}
    	this.killsBoardManager.registerParser(BukkitParsers::parseMaxPlayers); // {max}
    	
    	KillEventsParsers keParsers = new KillEventsParsers(this);
    	this.killsBoardManager.registerParser(keParsers::parsePoints); // {hpoints}, {dpoints}, {wpoints}
    	this.killsBoardManager.registerParser(keParsers::parseTimeRemaining); // {htime}, {dtime}, {wtime}
    	
    	DefianceCraftParsers dcParsers = new DefianceCraftParsers(this);
    	this.killsBoardManager.registerParser(dcParsers::parseEconomyBalance); // {tokens}
    	
    	FactionsParsers factionsParsers = new FactionsParsers();
    	this.killsBoardManager.registerParser(factionsParsers::parseFactionPower); // {fpower}, {fmaxpower}
    	
    	// Create leaderboard sign manager
    	this.leaderboardSignManager = new LeaderboardSignManager(this);
    	
    	// Register listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerListener(this), this);
    	pm.registerEvents(new BlockListener(this), this);
    	
    	// Start task for events ending
    	this.eventEndTask = new EventEndTask(this);
    	this.eventEndTask.runTaskTimer(
			this,
			EventType.HOURLY.getTicks() - this.config.cache.hourlyElapsed, // Remaining ticks until hourly event runs
			EventType.HOURLY.getTicks() // Run every hour
		);
    	
    	// Start task for countdown signs
    	new UpdateCountdownSignsTask(this).runTaskTimer(this, 20 - (config.cache.hourlyElapsed % 20), 20);
    	
    	// Start task for leaderboard signs
    	new UpdateLeaderboardSignsTask(this).runTaskTimer(this, 0, config.leaderboardUpdateFrequencyTicks);
    	
    	// Schedule messages
    	this.scheduleMessages(EventType.HOURLY);
    	this.scheduleMessages(EventType.DAILY);
    	this.scheduleMessages(EventType.WEEKLY);
    	
    	// Register commands
    	AdminCommands adminCmds = new AdminCommands(this);
    	CommandRegistry.registerPlayerCommand(this, "killevents", "defiancecraft.killevents.help", adminCmds::help);
    	CommandRegistry.registerPlayerSubCommand("killevents", "signwall", "defiancecraft.killevents.signwall", adminCmds::signWall);
    	CommandRegistry.registerPlayerSubCommand("killevents", "setregion", "defiancecraft.killevents.setregion", adminCmds::setRegion);
    
    }
    
	@Override
    public void onDisable() {

    	// Cancel tasks
    	this.eventEndTask.shutdown();
    	this.eventEndTask.cancel();
    	
    	// Save config
    	if (!this.saveConfig(config))
    		this.getLogger().severe("Failed to save configuration. Event timing will likely be corrupted. See console for any possible errors.");

    	// Save kill tracker
		try {
			this.tracker.save();
		} catch (Exception e) {
			this.getLogger().severe("Failed to save kills. Player kills will be reverted to the last successful save next time the server starts. See stack trace below.");
			e.printStackTrace();
		}
		
    }

    @Override
    public String getCanonicalName() {
        return "KillEvents";
    }

    @Override
    public Collection[] getCollections() {
        return new Collection[] {};
    }
	
    // -------------
    // Helper methods
    // -------------
    
    private boolean setupVault() {
    	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    	if (economyProvider != null)
    		this.economy = economyProvider.getProvider();
    	return (this.economy != null);
    }

    private void scheduleMessages(EventType type) {
    	EventConfig eventConfig = config.getEventConfig(type);
    	for (Entry<Integer, String> entry : eventConfig.messages.entrySet()) {
    		
    		int messageTicks = entry.getKey() * 20; // How many ticks into the event the message is supposed to execute at
    		int elapsedTicks = 0;
    		
    		// Calculate elapsed time into the event
    		switch (type) {
    		case HOURLY: elapsedTicks = config.cache.hourlyElapsed; break;
    		case DAILY:  elapsedTicks = config.cache.dailyHours * 60 * 60 * 20 + config.cache.hourlyElapsed; break;
    		case WEEKLY: elapsedTicks = config.cache.weeklyHours * 60 * 60 * 20 + config.cache.hourlyElapsed; break;
    		default: break;
    		}
    		
    		int difference = messageTicks - elapsedTicks;
    		
    		// Continue if message has already executed
    		if (difference < 0)
    			continue;
    		
    		new BroadcastMessageTask(entry.getValue()).runTaskTimer(this, difference, type.getTicks());
    		
    	}
    }
    
    // -------------
    // Getters
    // -------------
    
    public PointStrategy getPointStrategy() {
		return pointStrategy;
	}
    
    public Economy getEconomy() {
    	return economy;
    }
    
    public KillEventsConfig getConfiguration() {
    	return config;
    }
    
    public KillTracker getTracker() {
    	return tracker;
    }
    
    public EventEndTask getEventEndTask() {
    	return eventEndTask;
    }
    
    public KillsBoardManager getKillsBoardManager() {
    	return killsBoardManager;
    }
    
    public LeaderboardSignManager getLeaderboardSignManager() {
    	return leaderboardSignManager;
    }
    
}
