package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;

import com.autcraft.acelytrafall.game.data.PlayerGameState;

public class PlayerPortalEnter implements Listener {
	
	@EventHandler
	public void OnPlayerPortalEnter(EntityPortalEnterEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player)e.getEntity();
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		if(arena == null) {
			return;
		}
		
		if(arena.GetPlayerGameState(player) == PlayerGameState.FLYING) {
			arena.UpdatePlayerGameState(player, PlayerGameState.WIN);
		}
	}
}
