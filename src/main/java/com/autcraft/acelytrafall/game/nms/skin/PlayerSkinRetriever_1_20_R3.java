package com.autcraft.acelytrafall.game.nms.skin;

import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.game.data.PlayerRecordData.PlayerSkinData;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.level.ServerPlayer;

public class PlayerSkinRetriever_1_20_R3 implements PlayerSkinRetriever {
	
	@Override
	public PlayerSkinData RetrieveSkinData(Player player) {
		PlayerSkinData skinData = new PlayerSkinData();
		
		ServerPlayer nmsPlayer = ((CraftPlayer)player).getHandle();
		Property skinProperty = nmsPlayer.getGameProfile().getProperties().get("textures").iterator().next();
		
		skinData.texture = skinProperty.value();
		skinData.signature = skinProperty.signature();
		
		return skinData;
	}
}
