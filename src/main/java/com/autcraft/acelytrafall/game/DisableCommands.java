package com.autcraft.acelytrafall.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.autcraft.acelytrafall.Path;

public class DisableCommands implements Listener {
	
	private List<String> allowedCommands = new ArrayList<String>();
	
	public DisableCommands() {
		LoadAllowedCommands();
	}
	
	@EventHandler
	public void OnCommand(PlayerCommandPreprocessEvent e) {
		Player player = e.getPlayer();
		
		if(!ArenaManager.GetInstance().IsPlayerPlaying(player)) {
			return;
		}
		
		for(String allowedCommand : allowedCommands) {
			if(e.getMessage().toLowerCase().startsWith(allowedCommand)) {
				return;
			}
		}
		
		e.setCancelled(true);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can't execute this command while in game."));
	}
	
	private void LoadAllowedCommands() {
		File allowedCommandsFile = new File(Path.ROOT_DIR, "allowed_commands.yml");
		if(allowedCommandsFile.exists()) {
			try(BufferedReader reader = new BufferedReader(new FileReader(allowedCommandsFile))) {
				String line;
				while((line = reader.readLine()) != null) {
					if(line.isEmpty()) {
						continue;
					}
					
					if(!line.startsWith("/")) {
						line = "/" + line;
					}
					
					allowedCommands.add(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
		allowedCommands.add("/acelytrafall");
	}
}
