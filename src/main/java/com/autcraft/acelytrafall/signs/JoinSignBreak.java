package com.autcraft.acelytrafall.signs;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class JoinSignBreak implements Listener {
	
	@EventHandler
	public void OnJoinSignBreak(BlockBreakEvent e) {
		if(e.getBlock() == null || e.getBlock().getState() == null || !(e.getBlock().getState() instanceof Sign sign)) {
			return;
		}
		
		String firstLine = ChatColor.stripColor(sign.getSide(Side.FRONT).getLine(0));
		if(!firstLine.equals("[ACElytraFall]")) {
			return;
		}
		
		Player player = e.getPlayer();
		if(!player.isOp() && !player.hasPermission("acelytrafall.admin")) {
			e.setCancelled(true);
			return;
		}
		
		String arenaName = ChatColor.stripColor(sign.getSide(Side.FRONT).getLine(1).toLowerCase());
		if(!ArenaManager.GetInstance().ExistsArena(arenaName)) {
			return;
		}
		
		Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
		arena.joinSignLocations.remove(e.getBlock().getLocation());
		arena.SaveArena();
		
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
				String.format("&bYou've broken a join sign for arena &7'%s'&b.", arenaName)));
	}
}
