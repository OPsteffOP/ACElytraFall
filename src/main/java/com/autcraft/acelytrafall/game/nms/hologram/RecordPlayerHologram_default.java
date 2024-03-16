package com.autcraft.acelytrafall.game.nms.hologram;

import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.game.data.PlayerGameData;
import com.autcraft.acelytrafall.game.data.PlayerRecordData;

public class RecordPlayerHologram_default extends RecordPlayerHologram {
	
	public RecordPlayerHologram_default(PlayerRecordData data, Player player) {
		super(data, player);
	}
	
	@Override
	public void Spawn() {}

	@Override
	public void SetVisible(boolean isVisible) {}
	
	@Override
	public void ShowFrame(PlayerGameData playerGameData, int elapsedDuration) {}
}
