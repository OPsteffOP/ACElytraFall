package com.autcraft.acelytrafall.game;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.Path;
import com.google.common.io.Files;

public class ArenaManager {
	
	private static ArenaManager instance = null;
	
	private Map<String, Arena> arenas = new HashMap<String, Arena>();
	
	public static ArenaManager GetInstance() {
		if(instance == null) {
			instance = new ArenaManager();
		}
		
		return instance;
	}
	
	public void ShutDown() {
		for(Arena arena : arenas.values()) {
			arena.KickAllPlayers();
		}
	}
	
	public void LoadArenas() {
		String arenaDirectoryPath = Paths.get(Path.ROOT_DIR, Path.ARENA_DIR).toString();
		File arenaDirectoryFile = new File(arenaDirectoryPath);
		
		if(!arenaDirectoryFile.exists()) {
			return;
		}
		
		for(File file : arenaDirectoryFile.listFiles()) {
			if(!file.isFile()) {
				continue;
			}
			
			String arenaName = Files.getNameWithoutExtension(file.getName());
			LoadArena(arenaName);
		}
	}
	
	public void LoadArena(String arenaName) {
		if(ExistsArena(arenaName)) {
			return;
		}
		
		Arena arena = Arena.LoadArena(arenaName);
		AddArena(arena);
	}
	
	public void AddArena(Arena arena) {
		arenas.put(arena.name.toLowerCase(), arena);
	}
	
	public void UnloadArena(String arenaName) {
		arenas.remove(arenaName.toLowerCase());
	}
	
	public Arena GetArena(String arenaName) {
		return arenas.get(arenaName.toLowerCase());
	}
	
	public Arena GetArena(Player player) {
		for(Arena arena : arenas.values()) {
			if(arena.HasPlayerJoined(player)) {
				return arena;
			}
		}
		
		return null;
	}
	
	public Set<String> GetArenaNames() {
		return arenas.keySet();
	}
	
	public boolean ExistsArena(String arenaName) {
		return arenas.containsKey(arenaName.toLowerCase());
	}
	
	public boolean IsPlayerPlaying(Player player) {
		for(Arena arena : arenas.values()) {
			if(arena.HasPlayerJoined(player)) {
				return true;
			}
		}
		
		return false;
	}
}
