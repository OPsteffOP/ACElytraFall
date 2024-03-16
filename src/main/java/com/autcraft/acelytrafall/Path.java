package com.autcraft.acelytrafall;

import java.nio.file.Paths;
import java.util.UUID;

public class Path {
	
	public static final String ROOT_DIR = "plugins/" + ACElytraFall.class.getSimpleName();
	public static final String ARENA_DIR = "Arenas";
	public static final String PLAYER_DIR = "PlayerData";
	
	public static String GetArenaFilePath(String arenaName) {
		return Paths.get(Path.ROOT_DIR, Path.ARENA_DIR, arenaName.toLowerCase() + ".yml").toString();
	}
	
	public static String GetPlayerFilePath(UUID uuid) {
		return Paths.get(Path.ROOT_DIR, Path.PLAYER_DIR, uuid.toString() + ".yml").toString();
	}
}
