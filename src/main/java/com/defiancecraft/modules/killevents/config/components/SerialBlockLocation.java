package com.defiancecraft.modules.killevents.config.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class SerialBlockLocation {

	public int x;
	public int y;
	public int z;
	
	public String world;
	
	public SerialBlockLocation(Location loc) {
		this(loc.getBlock());
	}
	
	public SerialBlockLocation(Block block) {
		this(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
	}
	
	public SerialBlockLocation(int x, int y, int z, String world) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.world = world;
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
	public Block toBlock() {
		return toLocation().getBlock();
	}
	
}
