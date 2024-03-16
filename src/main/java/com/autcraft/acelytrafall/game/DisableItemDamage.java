package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class DisableItemDamage implements Listener {
	
	@EventHandler
	public void OnPlayerItemDamage(PlayerItemDamageEvent e) {
		Player player = e.getPlayer();
		
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
		}
	}
}
