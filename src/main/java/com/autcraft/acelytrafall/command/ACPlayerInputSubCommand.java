package com.autcraft.acelytrafall.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class ACPlayerInputSubCommand extends ACInputSubCommand {

	@Override
	protected List<String> GetInputOptions() {
		List<String> inputOptions = new ArrayList<String>();
		
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			inputOptions.add(onlinePlayer.getName());
		}
		
		return inputOptions;
	}
}
