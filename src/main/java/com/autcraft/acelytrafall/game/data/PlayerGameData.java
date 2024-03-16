package com.autcraft.acelytrafall.game.data;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.autcraft.acelytrafall.configuration.ACConfig;
import com.autcraft.acelytrafall.configuration.ACConfigCloneable;
import com.autcraft.acelytrafall.configuration.ConfigField;
import com.autcraft.acelytrafall.configuration.ConfigField.FieldConversion;
import com.autcraft.acelytrafall.game.data.PlayerRecordData.TimedPlayerState;
import com.autcraft.acelytrafall.game.nms.hologram.RecordPlayerHologram;
import com.autcraft.acelytrafall.game.scoreboard.ACScoreboard;

import net.md_5.bungee.api.ChatColor;

public class PlayerGameData implements ACConfigCloneable<PlayerGameData> {
	public static enum RecordHologramViewType {
		NONE,
		PERSONAL_RECORD,
		GLOBAL_RECORD
	}
	
	public static class PlayerConfig {
		@ConfigField(conversion=FieldConversion.ENUM_TO_STRING) public RecordHologramViewType recordHologramViewType = RecordHologramViewType.PERSONAL_RECORD;
	}
	
	@ConfigField public PlayerConfig personalConfig = null;
	@ConfigField public PlayerRecordData personalRecordData = null;
	
	public ACConfig config = null;
	
	public Location beforeJoinLocation = null;
	public ItemStack[] beforeJoinInventoryContents = null;
	public boolean beforeJoinAllowFlight = false;
	public GameMode beforeJoinGameMode = null;
	
	public ACScoreboard scoreboard = null;
	
	public long joinTime = Integer.MIN_VALUE;
	public int tickTask = -1;
	public List<TimedPlayerState> movementHistory = new ArrayList<TimedPlayerState>();
	
	public int lastRecordPlayerHologramMovementHistoryIndex = 0;
	public RecordPlayerHologram recordPlayerHologram = null;
	
	public boolean isFlying = false;
	
	public String GetPlayerDataString() {
		String recordHologramView = personalConfig.recordHologramViewType.toString();
		String recordDuration = (personalRecordData == null) ? "&cN/A" : String.format("%.2f", personalRecordData.duration / 1000.f);
		return ChatColor.translateAlternateColorCodes('&', String.format(
				"&7Record hologram view: &6%s\n" + 
				"&7Record time: &6%ss"
			, recordHologramView, recordDuration));
	}
	
	public boolean IsTimerStarted() {
		return joinTime != Integer.MIN_VALUE;
	}
}
