package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import com.autcraft.acelytrafall.game.data.PlayerGameState;

public class PlayerElytraToggle implements Listener {
	
	@EventHandler
	public void OnPlayerElytraToggle(EntityToggleGlideEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player)e.getEntity();
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		if(arena == null) {
			return;
		}
		
		if(e.isGliding() && arena.GetPlayerGameState(player) == PlayerGameState.IDLE) {
			arena.UpdatePlayerGameState(player, PlayerGameState.FLYING);
		}
	}
}
