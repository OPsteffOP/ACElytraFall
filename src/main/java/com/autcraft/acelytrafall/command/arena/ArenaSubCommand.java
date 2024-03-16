package com.autcraft.acelytrafall.command.arena;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.autcraft.acelytrafall.command.ACSubCommand;

public class ArenaSubCommand extends ACSubCommand {
	
	private static final String SUB_COMMAND_NAME = "arena";
	
	public ArenaSubCommand() {
		AddSubCommand(new ArenaCreateSubCommand());
		AddSubCommand(new ArenaDeleteSubCommand());
	}
	
	@Override
	protected String GetSubCommandName() {
		return SUB_COMMAND_NAME;
	}
	
	@Override
	protected boolean ValidateCommandSender(CommandSender sender) {
		return true;
	}

	@Override
	protected boolean OnCommand(CommandSender sender, List<String> inputs) {
		return false;
	}
}
