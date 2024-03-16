package com.autcraft.acelytrafall.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class PlayerJoin implements Listener {
	
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		ItemStack chestplate = player.getInventory().getChestplate();
		if(chestplate == null) {
			return;
		}
		
		ItemMeta chestplateMeta = chestplate.getItemMeta();
		if(chestplateMeta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&6&l[ACElytraFall] &7Elytra"))) {
			player.getInventory().setChestplate(null);
		}
	}
}
