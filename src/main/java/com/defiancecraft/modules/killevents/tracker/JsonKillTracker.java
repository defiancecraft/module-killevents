package com.defiancecraft.modules.killevents.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.defiancecraft.modules.killevents.util.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * A Kill Tracker implementation that uses a JSON file to
 * track kills.
 */
public class JsonKillTracker implements KillTracker {

	protected static final Gson GSON = new GsonBuilder()
		.disableHtmlEscaping()
		.create();
	
	protected SerialKillTracker cache;
	protected File file;
	protected boolean gzip;
	
	/**
	 * Constructs a new JsonKillTracker for the given input file
	 * 
	 * @param file File to read/write to
	 * @param gzip Whether the file is/should be gzipped
	 */
	public JsonKillTracker(File file, boolean gzip) {
		
		this.file = file;
		this.gzip = gzip;
		
		InputStream in = null;
		
		try {
			
			// Create parent directories and new file if not found;
			// Don't bother trying to parse in this case, as file will be blank.
			if (!file.exists()) {
				if (file.getParentFile() != null && !file.getParentFile().isDirectory())
					file.getParentFile().mkdirs();
				file.createNewFile();
				cache = new SerialKillTracker();
				return;
			}

			if (file.length() == 0) {
				cache = new SerialKillTracker();
				return;
			}
			
			in = new FileInputStream(file);
			if (gzip)
				in = new GZIPInputStream(in);
			
			// Try and read the file
			cache = GSON.fromJson(new InputStreamReader(in), SerialKillTracker.class);
			
		} catch (IOException | JsonParseException e) {
			
			System.err.printf("Failed to read/parse JSON kill tracker '%s': %s. The file will be ignored.\n",
				file.getAbsolutePath(),
				e.getMessage());
			
		} finally {
			
			// Close input stream
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {}
			
			// Create cache as empty if file was not read
			if (cache == null)
				cache = new SerialKillTracker();
			 
		}
		
	}
	
	@Override
	public int getKills(UUID player, EventType event) {
		Map<UUID, Integer> map = cache.getMap(event);
		return map.containsKey(player) ? map.get(player) : 0;
	}

	@Override
	public boolean setKills(UUID player, EventType event, int kills) {
		cache.getMap(event).put(player, kills);
		return true;
	}

	@Override
	public boolean resetKills(UUID player, EventType event) {
		cache.getMap(event).put(player, 0);
		return true;
	}

	@Override
	public Map<UUID, Integer> getEventKills(EventType event) {
		return new HashMap<UUID, Integer>(cache.getMap(event));
	}

	@Override
	public boolean resetEventKills(EventType event) {
		cache.getMap(event).clear();
		return true;
	}
	
	@Override
	public void save() throws IOException {

		// Open output stream (either GZIP or File)
		OutputStream out = new FileOutputStream(file);
		if (gzip)
			out = new GZIPOutputStream(out);
		
		// Write data
		GSON.toJson(cache, new OutputStreamWriter(out));
		
		// Close output stream
		out.close();
		
	}
	
	protected class SerialKillTracker {
		public Map<UUID, Integer> hourlyKills = new HashMap<>();
		public Map<UUID, Integer> dailyKills = new HashMap<>();
		public Map<UUID, Integer> weeklyKills = new HashMap<>();
		
		public Map<UUID, Integer> getMap(EventType type) {
			switch (type) {
			case HOURLY: return hourlyKills;
			case DAILY: return dailyKills;
			case WEEKLY: return weeklyKills;
			default: return null;
			}
		}
	}
	
}
