package com.autcraft.acelytrafall.command.arena;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.command.ACEnumInputSubCommand;
import com.autcraft.acelytrafall.command.ACPlaceholderSubCommand;
import com.autcraft.acelytrafall.command.ACSubCommand;
import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;
import com.autcraft.acelytrafall.game.data.PlayerGameData;
import com.autcraft.acelytrafall.game.data.PlayerGameData.RecordHologramViewType;

import net.md_5.bungee.api.ChatColor;

public class ConfigSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "config";
	
	public ConfigSubCommand() {
		AddSubCommand(new RecordHologramSubCommand());
	}

	@Override
	protected String GetSubCommandName() {
		return SUB_COMMAND_NAME;
	}

	@Override
	protected boolean ValidateCommandSender(CommandSender sender) {
		if(!(sender instanceof Player player)) {
			return false;
		}
		
		return ArenaManager.GetInstance().IsPlayerPlaying(player);
	}

	@Override
	protected boolean OnCommand(CommandSender sender, List<String> inputs) {
		return false;
	}
	
	private class RecordHologramSubCommand extends ACPlaceholderSubCommand {
		
		private static final String SUB_COMMAND_NAME = "record_hologram";
		
		public RecordHologramSubCommand() {
			AddSubCommand(new RecordHologramValueSubCommand());
		}

		@Override
		protected String GetSubCommandName() {
			return SUB_COMMAND_NAME;
		}
	}
	
	private class RecordHologramValueSubCommand extends ACEnumInputSubCommand<RecordHologramViewType> {

		@Override
		protected boolean ValidateCommandSender(CommandSender sender) {
			return true;
		}

		@Override
		protected boolean OnCommand(CommandSender sender, List<String> inputs) {
			Player player = (Player)sender;
			String recordHologramViewTypeStr = inputs.get(0);
			RecordHologramViewType recordHologramViewType = RecordHologramViewType.valueOf(recordHologramViewTypeStr);
			
			Arena arena = ArenaManager.GetInstance().GetArena(player);
			PlayerGameData playerGameData = arena.GetPlayerGameData(player);
			playerGameData.personalConfig.recordHologramViewType = recordHologramViewType;
			arena.SavePlayer(player);
			
			arena.OnPlayerRecordHologramViewChange(player, playerGameData);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&aYou've changed your &7'record_hologram' &aconfiguration to &7'%s'&a.", recordHologramViewType.toString())));
			
			return true;
		}
	}
}
