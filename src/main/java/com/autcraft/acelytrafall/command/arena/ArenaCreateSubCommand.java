package com.autcraft.acelytrafall.command.arena;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.command.ACInputSubCommand;
import com.autcraft.acelytrafall.command.ACSubCommand;
import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class ArenaCreateSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "create";
	
	public ArenaCreateSubCommand() {
		AddSubCommand(new ArenaNameSubCommand());
	}

	@Override
	protected String GetSubCommandName() {
		return SUB_COMMAND_NAME;
	}
	
	@Override
	protected boolean ValidateCommandSender(CommandSender sender) {
		if(!(sender instanceof Player)) {
			return false;
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
			return null;
		}
		
		@Override
		protected boolean ValidateCommandSender(CommandSender sender) {
			return true;
		}

		@Override
		protected boolean OnCommand(CommandSender sender, List<String> inputs) {
			Player player = (Player)sender;
			String arenaName = inputs.get(0);
			
			if(ArenaManager.GetInstance().ExistsArena(arenaName)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
						"&cAn arena with name &7'%s' &calready exists.", arenaName)));
				return true;
			}
			
			Arena arena = new Arena();
			arena.name = arenaName;
			arena.startLocation = player.getLocation();
			arena.joinSignLocations = new ArrayList<Location>();
			arena.SaveArena();
			
			ArenaManager.GetInstance().AddArena(arena);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&aYou've created an arena with name '%s'.", arenaName)));
			
			return true;
		}
	}
}
