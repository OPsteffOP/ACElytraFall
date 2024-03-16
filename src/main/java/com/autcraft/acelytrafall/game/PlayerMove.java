package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.autcraft.acelytrafall.game.data.PlayerGameState;

public class PlayerMove implements Listener {
	
	@EventHandler
	public void OnPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		if(arena == null) {
			return;
		}
		
		if(arena.GetPlayerGameState(player) == PlayerGameState.FLYING && !player.isGliding()) {
			arena.UpdatePlayerGameState(player, PlayerGameState.LOSE);
		}
	}
}
