package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.autcraft.acelytrafall.game.data.PlayerGameState;

public class DisablePlayerDamage implements Listener {
	
	@EventHandler
	public void OnPlayerDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player)e.getEntity();
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		if(arena == null) {
			return;
		}
		
		e.setCancelled(true);
		
		if(e.getCause() == DamageCause.FLY_INTO_WALL || e.getCause() == DamageCause.FALL) {
			arena.UpdatePlayerGameState(player, PlayerGameState.LOSE);
		}
	}
}
