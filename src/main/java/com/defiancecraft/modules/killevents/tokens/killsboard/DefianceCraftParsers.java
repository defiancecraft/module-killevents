package com.defiancecraft.modules.killevents.tokens.killsboard;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.core.database.Database;
import com.defiancecraft.core.database.collections.Users;
import com.defiancecraft.core.database.documents.DBUser;
import com.defiancecraft.modules.killevents.KillEvents;
import com.defiancecraft.modules.killevents.tokens.TokenParserFunction;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class DefianceCraftParsers {

	private final KillEvents plugin;
	private UpdateBalanceCacheTask updateTask;
	private volatile Map<UUID, Double> balanceCache = new ConcurrentHashMap<>();
	
	public DefianceCraftParsers(KillEvents plugin) {
		this.plugin = plugin;
		this.updateTask = new UpdateBalanceCacheTask();
		this.updateTask.runTaskTimerAsynchronously(plugin, 0L, plugin.getConfiguration().scoreboard.economyTicks);
	}
	
	@TokenParserFunction
	public String parseEconomyBalance(String input, Player player) {
		if (!input.contains("{tokens}"))
			return input;
		
		UUID uuid = player.getUniqueId();
		return input.replace("{tokens}", balanceCache.containsKey(uuid) ? balanceCache.get(uuid).toString() : "0");
	}
	
	class UpdateBalanceCacheTask extends BukkitRunnable {
		
		@Override
		public void run() {
			synchronized (balanceCache) {
				
				Set<UUID> players = plugin.getKillsBoardManager().getRegisteredPlayers();
				DBCollection collection = Database.getCollection(Users.class).getDBC();
				
				// Don't bother querying if there are no players 
				if (players.size() < 1)
					return;
				
				// Find players where their UUID is in the list of players
				DBCursor cur = collection.find(new BasicDBObject("uuid", new BasicDBObject("$in", players)));
				
				// Iterate over each found player and put their balance in cache
				while (cur.hasNext()) {
					DBUser user = new DBUser(cur.next());
					balanceCache.put(user.getUUID(), user.getBalance());
				}
				
				// Clean up cache by removing players that no longer exist
				Iterator<Entry<UUID, Double>> it = balanceCache.entrySet().iterator();
				while (it.hasNext()) {
					Entry<UUID, Double> entry = it.next();
					if (!players.contains(entry.getKey()))
						it.remove();
				}
						
			}
		}
		
	}
			
}
