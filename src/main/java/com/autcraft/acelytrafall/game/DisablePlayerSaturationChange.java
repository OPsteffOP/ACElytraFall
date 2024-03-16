package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DisablePlayerSaturationChange implements Listener {
	
	@EventHandler
	public void OnSaturationChange(FoodLevelChangeEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player)e.getEntity();
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
		}
	}
}
