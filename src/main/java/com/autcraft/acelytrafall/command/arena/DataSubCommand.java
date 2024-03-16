package com.autcraft.acelytrafall.command.arena;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.command.ACInputSubCommand;
import com.autcraft.acelytrafall.command.ACPlayerInputSubCommand;
import com.autcraft.acelytrafall.command.ACSubCommand;
import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;
import com.autcraft.acelytrafall.game.data.PlayerGameData;

import net.md_5.bungee.api.ChatColor;

public class DataSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "data";
	
	public DataSubCommand() {
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
		
		public ArenaNameSubCommand() {
			AddSubCommand(new TargetNameSubCommand());
		}
		
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
			String arenaName = inputs.get(0);
			
			Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
			int playerCount = arena.playerGameDataList.size();
			String recordHolder = (arena.recordData == null) ? "&cN/A" : Bukkit.getOfflinePlayer(arena.recordData.holder).getName();
			String recordDuration = (arena.recordData == null) ? "&cN/A" : String.format("%.2f", arena.recordData.duration / 1000.f);
			
			String dataString = ChatColor.translateAlternateColorCodes('&', String.format(
					"&7Currently playing: &6%d\n" + 
					"&7Record holder: &6%s\n" + 
					"&7Record time: &6%ss"
				, playerCount, recordHolder, recordDuration));
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&b===== %1$s =====\n%2$s&b\n===== %1$s =====", arena.name, dataString)));
			return true;
		}
	}
	
	private class TargetNameSubCommand extends ACPlayerInputSubCommand {
		
		@Override
		protected boolean ShouldValidateInputOnCommand() {
			return false;
		}

		@Override
		protected boolean ValidateCommandSender(CommandSender sender) {
			return true;
		}

		@Override
		protected boolean OnCommand(CommandSender sender, List<String> inputs) {
			String arenaName = inputs.get(0);
			String targetName = inputs.get(1);
			
			@SuppressWarnings("deprecation")
			OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
			if(offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
						"&cThe target with the name &7'%s' &ccouldn't be found.", targetName)));
				return true;
			}
			
			Arena arena = ArenaManager.GetInstance().GetArena(arenaName);
			PlayerGameData targetGameData = arena.LoadPlayer(offlineTarget.getUniqueId());
			
			String dataString = targetGameData.GetPlayerDataString();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&b===== %1$s =====\n%2$s&b\n===== %1$s =====", offlineTarget.getName(), dataString)));
			return true;
		}
	}
}
