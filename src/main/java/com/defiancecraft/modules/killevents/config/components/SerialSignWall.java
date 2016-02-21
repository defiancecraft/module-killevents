package com.defiancecraft.modules.killevents.config.components;

import org.bukkit.Bukkit;

import com.defiancecraft.modules.killevents.util.EventType;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class SerialSignWall {

	public SerialBlockLocation a;
	public SerialBlockLocation b;
	public EventType type;
	
	public SerialSignWall(Selection sel, EventType type) {
		if (Math.max(sel.getWidth(), sel.getLength()) != 2
				|| Math.min(sel.getWidth(), sel.getLength()) != 1) {
			throw new IllegalArgumentException("Selection must be 2D surface with width of 2.");
		}
		
		this.a = new SerialBlockLocation(sel.getMaximumPoint());
		this.b = new SerialBlockLocation(sel.getMinimumPoint());
		this.type = type;
	}
	
	public Selection toSelection() {
		return new CuboidSelection(Bukkit.getWorld(a.world), a.toLocation(), b.toLocation());
	}
	
}
