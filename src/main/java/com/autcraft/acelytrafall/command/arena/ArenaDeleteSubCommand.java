package com.autcraft.acelytrafall.command.arena;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.Path;
import com.autcraft.acelytrafall.command.ACInputSubCommand;
import com.autcraft.acelytrafall.command.ACSubCommand;
import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class ArenaDeleteSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "delete";
	
	public ArenaDeleteSubCommand() {
		AddSubCommand(new ArenaNameSubCommand());
	}

	@Override
	protected String GetSubCommandName() {
		return SUB_COMMAND_NAME;
	}
	
	@Override
	protected boolean ValidateCommandSender(CommandSender sender) {
		if(!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player)sender;
		if(!player.isOp() && !player.hasPermission("acelytrafall.admin")) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean OnCommand(CommandSender sender, List<String> inputs) {
		return false;
	}
	
	private class ArenaNameSubCommand extends ACInputSubCommand {
		
		@Override
		protected List<String> GetInputOptions() {
			return List.copyOf(ArenaManager.GetInstance().GetArenaNames());
		}
		
		@Override
		protected boolean ValidateCommandSender(CommandSender sender) {
			return true;
		}

		@Override
		protected boolean OnCommand(CommandSender sender, List<String> inputs) {
			Player player = (Player)sender;
			String arenaName = inputs.get(0);
			
			if(!ArenaManager.GetInstance().ExistsArena(arenaName)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
						"&cAn arena with name &7'%s' &cdoesn't exist.", arenaName)));
				return true;
			}
			
			Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
			for(Location joinSignLocation : arena.joinSignLocations) {
				Block block = joinSignLocation.getBlock();
				if(block != null && block.getState() != null && block.getState() instanceof Sign) {
					block.setType(Material.AIR);
				}
			}
			
			for(UUID playerUUID : arena.playerGameDataList.keySet()) {
				arena.OnPlayerLeave(Bukkit.getPlayer(playerUUID));
			}
			
			ArenaManager.GetInstance().UnloadArena(arenaName);
			
			String arenaFilePath = Path.GetArenaFilePath(arenaName);
			File arenaFile = new File(arenaFilePath);
			arenaFile.delete();
			
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&aYou've deleted the arena with name '%s'.", arenaName)));
			return true;
		}
	}
}
