package com.autcraft.acelytrafall.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ACCommand extends ACSubCommand implements TabExecutor {
	
	public static void Register(JavaPlugin plugin, ACCommand command) {
		plugin.getCommand(command.GetSubCommandName()).setExecutor(command);
		plugin.getCommand(command.GetSubCommandName()).setTabCompleter(command);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> arguments = new ArrayList<String>(args.length + 1);
		arguments.add(label);
		Collections.addAll(arguments, args);
		
		return HandleCommand(sender, arguments, new ArrayList<String>(), this);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> suggestions = new ArrayList<String>();
		
		ACSubCommand currentSubCommand = this;
		for(int i = 0; i < args.length - 1; ++i) {
			boolean foundSubCommand = false;
			for(ACSubCommand subCommand : currentSubCommand.subCommands) {
				if(args[i].equalsIgnoreCase(subCommand.GetSubCommandName()) || 
						(subCommand instanceof ACInputSubCommand inputSubCommand && inputSubCommand.ValidateInput(args[i]))) {
					currentSubCommand = subCommand;
					foundSubCommand =  true;
					break;
				}
			}
			
			if(!foundSubCommand) {
				return suggestions;
			}
		}
		
		String startCompletion = args[args.length - 1].toLowerCase();
		
		Set<String> alreadyIncludedInputOptions = new HashSet<String>();
		for(ACSubCommand subCommand : currentSubCommand.subCommands) {
			if(!subCommand.ValidateCommandSender(sender)) {
				continue;
			}
			
			if(subCommand instanceof ACInputSubCommand inputSubCommand) {
				List<String> inputOptions = inputSubCommand.GetInputOptions();
				if(inputOptions == null) {
					continue;
				}
				
				for(String inputOption : inputOptions) {
					if(alreadyIncludedInputOptions.contains(inputOption)) {
						continue;
					}
					
					if(inputOption.toLowerCase().startsWith(startCompletion)) {
						suggestions.add(inputOption);
					}
					alreadyIncludedInputOptions.add(inputOption);
				}
			} else {
				String subCommandName = subCommand.GetSubCommandName();
				if(subCommandName.toLowerCase().startsWith(startCompletion)) {
					suggestions.add(subCommandName);
				}
			}
		}
		
		return suggestions;
	}
}
