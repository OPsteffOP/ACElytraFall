package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DisableInventoryChange implements Listener {
	
	@EventHandler
	public void OnInventoryChange(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		
		Player player = (Player)e.getWhoClicked();
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
		}
	}
}
