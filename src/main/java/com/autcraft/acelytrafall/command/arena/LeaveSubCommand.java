package com.autcraft.acelytrafall.command.arena;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.command.ACSubCommand;
import com.autcraft.acelytrafall.game.Arena;
import com.autcraft.acelytrafall.game.ArenaManager;

public class LeaveSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "leave";

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
		Player player = (Player)sender;
		Arena arena = ArenaManager.GetInstance().GetArena(player);
		
		arena.OnPlayerLeave(player);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou have left the ACElytraFall game."));
		return true;
	}
}
