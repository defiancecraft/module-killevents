package com.defiancecraft.modules.killevents.config.components;

import org.bukkit.Bukkit;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class SerialRegion {

	public SerialBlockLocation a;
	public SerialBlockLocation b;
	
	public SerialRegion(Selection sel) {
		this.a = new SerialBlockLocation(sel.getMinimumPoint());
		this.b = new SerialBlockLocation(sel.getMaximumPoint());
	}
	
	public Selection toSelection() {
		return new CuboidSelection(Bukkit.getWorld(a.world), a.toLocation(), b.toLocation());
	}
	
}
