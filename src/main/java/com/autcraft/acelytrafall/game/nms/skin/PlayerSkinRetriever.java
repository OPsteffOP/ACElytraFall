package com.autcraft.acelytrafall.game.nms.skin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.game.data.PlayerRecordData.PlayerSkinData;

public interface PlayerSkinRetriever {
	
	public PlayerSkinData RetrieveSkinData(Player player);
	
	public static PlayerSkinRetriever CreatePlayerSkinRetriever() {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		if(version.equals("v1_20_R3")) {
			return new PlayerSkinRetriever_1_20_R3();
		}
		
		return new PlayerSkinRetriever_default();
	}
}
