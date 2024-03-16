package com.autcraft.acelytrafall.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class ACPlaceholderSubCommand extends ACSubCommand {

	@Override
	protected boolean ValidateCommandSender(CommandSender sender) {
		return true;
	}

	@Override
	protected boolean OnCommand(CommandSender sender, List<String> inputs) {
		return false;
	}
}
