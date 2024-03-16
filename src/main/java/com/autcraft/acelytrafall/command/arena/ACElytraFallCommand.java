package com.autcraft.acelytrafall.command.arena;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.autcraft.acelytrafall.command.ACCommand;

public class ACElytraFallCommand extends ACCommand {
	
	private static final String COMMAND_NAME = "acelytrafall";
	
	public ACElytraFallCommand() {
		AddSubCommand(new ArenaSubCommand());
		AddSubCommand(new LeaveSubCommand());
		AddSubCommand(new ConfigSubCommand());
		AddSubCommand(new DataSubCommand());
	}

	@Override
	protected String GetSubCommandName() {
		return COMMAND_NAME;
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
