package com.autcraft.acelytrafall.game.data;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import com.autcraft.acelytrafall.configuration.ConfigField;
import com.autcraft.acelytrafall.configuration.ConfigField.FieldConversion;

public class PlayerRecordData {
	public static class PlayerSkinData {
		@ConfigField public String texture = null;
		@ConfigField public String signature = null;
	}
	
	public static class TimedPlayerState {
		@ConfigField public int elapsedDuration = -1;
		@ConfigField public Location location = null;
		@ConfigField public boolean isGliding = false;
	}
	
	@ConfigField(conversion=FieldConversion.UUID_TO_STRING) public UUID holder = null;
	@ConfigField(name="holder_skindata") public PlayerSkinData skinData = null;
	@ConfigField public int duration = Integer.MAX_VALUE;
	@ConfigField public List<TimedPlayerState> movementHistory = null;
}
