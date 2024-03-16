package com.autcraft.acelytrafall.signs;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class JoinSignClick implements Listener {

	@EventHandler
	public void OnJoinSignClick(PlayerInteractEvent e) {
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		if(e.getClickedBlock() == null || !(e.getClickedBlock().getState() instanceof Sign)) {
			return;
		}
		
		Player player = e.getPlayer();
		Sign sign = (Sign)e.getClickedBlock().getState();
		
		String firstLine = ChatColor.stripColor(sign.getSide(Side.FRONT).getLine(0));
		if(!firstLine.equals("[ACElytraFall]")) {
			return;
		}
		
		if(ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			e.setCancelled(true);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are already playing."));
			return;
		}
		
		String arenaName = ChatColor.stripColor(sign.getSide(Side.FRONT).getLine(1));
		Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
		
		if(arena == null) {
			e.setCancelled(true);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cArena doesn't exist."));
			return;
		}
		
		e.setCancelled(true);
		arena.OnPlayerJoin(player);
	}
}
