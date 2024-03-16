package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class DisableBlockInteraction implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnBlockPlace(BlockPlaceEvent e) {
		Player player = e.getPlayer();
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
		}
	}
}
