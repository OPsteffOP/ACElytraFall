package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.autcraft.acelytrafall.game.data.PlayerGameData;

public class PlayerLeave implements Listener {
	
	@EventHandler
	public void OnPlayerLeave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		if(arena == null) {
			return;
		}
		
		PlayerGameData playerGameData = arena.GetPlayerGameData(player);
		player.teleport(playerGameData.beforeJoinLocation);
		
		arena.OnPlayerLeave(player);
	}
}
