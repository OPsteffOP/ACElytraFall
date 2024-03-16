package com.autcraft.acelytrafall.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class ACSubCommand {
	
	protected List<ACSubCommand> subCommands = new ArrayList<ACSubCommand>();
	
	protected abstract String GetSubCommandName();
	
	protected abstract boolean ValidateCommandSender(CommandSender sender);
	protected abstract boolean OnCommand(CommandSender sender, List<String> inputs);
	
	public boolean HandleCommand(CommandSender sender, List<String> args, List<String> inputs, ACCommand originalCmd) {
		args.remove(0);
		
		if(args.size() == 0) {
			if(!OnCommand(sender, inputs)) {
				ShowHelpMessage(sender, originalCmd);
			}
			return true;
		}
		
		String name = args.get(0);
		for(ACSubCommand subCommand : subCommands) {
			if(!subCommand.ValidateCommandSender(sender)) {
				continue;
			}
			
			if(subCommand instanceof ACInputSubCommand) {
				ACInputSubCommand inputSubCommand = (ACInputSubCommand)subCommand;
				if(!inputSubCommand.ShouldValidateInputOnCommand() || inputSubCommand.ValidateInput(name)) {
					inputs.add(name);
					return inputSubCommand.HandleCommand(sender, args, inputs, originalCmd);
				}
			} else {
				if(name.equalsIgnoreCase(subCommand.GetSubCommandName())) {
					return subCommand.HandleCommand(sender, args, inputs, originalCmd);
				}
			}
		}
		
		ShowHelpMessage(sender, originalCmd);
		return true;
	}
	
	public void AddSubCommand(ACSubCommand subCommand) {
		subCommands.add(subCommand);
		subCommands.sort(new Comparator<ACSubCommand>() {

			@Override
			public int compare(ACSubCommand subCommand1, ACSubCommand subCommand2) {
				if(!(subCommand1 instanceof ACInputSubCommand) && subCommand2 instanceof ACInputSubCommand) {
					return -1;
				} else if(subCommand1 instanceof ACInputSubCommand && !(subCommand2 instanceof ACInputSubCommand)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
	}
	
	private void ShowHelpMessage(CommandSender sender, ACCommand originalCmd) {
		String usageMessage = ConstructHelpMessage(sender, "", originalCmd);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b===== Command Usage =====\n"
				+ usageMessage + "&b\n===== Command Usage ====="));
	}
	
	private String ConstructHelpMessage(CommandSender sender, String prefix, ACSubCommand subCommand) {
		if(subCommand.subCommands.isEmpty()) {
			return "&7- " + prefix + " " + subCommand.GetSubCommandName();
		}
		
		StringBuilder usageMessage = new StringBuilder();
		String newPrefix = prefix + " " + subCommand.GetSubCommandName();
		for(ACSubCommand nextSubCommand : subCommand.subCommands) {
			if(!nextSubCommand.ValidateCommandSender(sender)) {
				continue;
			}
			
			if(!usageMessage.isEmpty()) {
				usageMessage.append("\n");
			}
			usageMessage.append(ConstructHelpMessage(sender, newPrefix, nextSubCommand));
		}
		
		return usageMessage.toString();
	}
}
