package com.autcraft.acelytrafall.signs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class JoinSignCreate implements Listener {
	
	@EventHandler
	public void OnJoinSignCreate(SignChangeEvent e) {
		String firstLine = ChatColor.stripColor(e.getLine(0));
		if(!firstLine.equals("[ACElytraFall]") || e.getLine(1).isEmpty()) {
			return;
		}
		
		Player player = e.getPlayer();
		if(!player.isOp() && !player.hasPermission("acelytrafall.admin")) {
			e.setCancelled(true);
			e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(e.getBlock().getType()));
			e.getBlock().setType(Material.AIR);
			return;
		}
		
		String arenaName = ChatColor.stripColor(e.getLine(1).toLowerCase());
		if(!ArenaManager.GetInstance().ExistsArena(arenaName)) {
			e.setCancelled(true);
			e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(e.getBlock().getType()));
			e.getBlock().setType(Material.AIR);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
					String.format("&cAn arena with name &7'%s' &cdoesn't exist.", arenaName)));
			return;
		}
		
		Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
		arenaName = arena.name;
		
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&6[&bACElytraFall&6]"));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&1" + arenaName));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', "&6[JOIN]"));
		
		if(!arena.joinSignLocations.contains(e.getBlock().getLocation())) {
			arena.joinSignLocations.add(e.getBlock().getLocation());
			arena.SaveArena();
		}
		
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
				String.format("&bYou've created a join sign for arena &7'%s'&b.", arenaName)));
	}
}
