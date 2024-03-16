package com.autcraft.acelytrafall.game.nms.hologram;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.game.data.PlayerGameData;
import com.autcraft.acelytrafall.game.data.PlayerRecordData;

public abstract class RecordPlayerHologram {
	
	protected PlayerRecordData data = null;
	protected Player player = null;
	protected boolean isVisible = false;
	
	public RecordPlayerHologram(PlayerRecordData data, Player player) {
		this.data = data;
		this.player = player;
	}
	
	public abstract void Spawn();
	public abstract void SetVisible(boolean isVisible);
	public abstract void ShowFrame(PlayerGameData playerGameData, int elapsedDuration);
	
	public void UpdateRecordData(PlayerRecordData data) {
		this.data = data;
	}
	
	public static RecordPlayerHologram CreateHologramPlayer(PlayerRecordData data, Player player) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		if(version.equals("v1_20_R3")) {
			return new RecordPlayerHologram_1_20_R3(data, player);
		}
		
		return new RecordPlayerHologram_default(data, player);
	}
}
