package com.defiancecraft.modules.killevents.config.components;

import org.bukkit.Location;

import com.defiancecraft.modules.killevents.util.EventType;

public class SerialCountdownSign {

	public SerialBlockLocation location;
	public EventType type;
	
	public SerialCountdownSign(Location loc, EventType type) {
		this.location = new SerialBlockLocation(loc);
		this.type = type;
	}
	
}
