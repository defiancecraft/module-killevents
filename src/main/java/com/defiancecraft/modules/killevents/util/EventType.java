package com.defiancecraft.modules.killevents.util;

public enum EventType {
	
	HOURLY(20 * 60 * 60),
	
	DAILY(20 * 60 * 60 * 24), 
	
	WEEKLY(20 * 60 * 60 * 24 * 7);
	
	private int ticks;
	
	/**
	 * Creates a new EventType
	 * @param ticks The number of ticks this event lasts for
	 */
	EventType(int ticks) {
		this.ticks = ticks;
	}
	
	public int getTicks() {
		return ticks;
	}
	
}
