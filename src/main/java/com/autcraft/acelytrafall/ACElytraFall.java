package com.autcraft.acelytrafall;

import org.bukkit.plugin.java.JavaPlugin;

import com.autcraft.acelytrafall.command.ACCommand;
import com.autcraft.acelytrafall.command.arena.ACElytraFallCommand;
import com.autcraft.acelytrafall.game.ArenaManager;
import com.autcraft.acelytrafall.game.DisableBlockInteraction;
import com.autcraft.acelytrafall.game.DisableCommands;
import com.autcraft.acelytrafall.game.DisableInventoryChange;
import com.autcraft.acelytrafall.game.DisableItemDamage;
import com.autcraft.acelytrafall.game.DisablePlayerDamage;
import com.autcraft.acelytrafall.game.DisablePlayerSaturationChange;
import com.autcraft.acelytrafall.game.PlayerElytraToggle;
import com.autcraft.acelytrafall.game.PlayerJoin;
import com.autcraft.acelytrafall.game.PlayerLeave;
import com.autcraft.acelytrafall.game.PlayerMove;
import com.autcraft.acelytrafall.game.PlayerPortalEnter;
import com.autcraft.acelytrafall.signs.JoinSignBreak;
import com.autcraft.acelytrafall.signs.JoinSignClick;
import com.autcraft.acelytrafall.signs.JoinSignCreate;

public class ACElytraFall extends JavaPlugin {
	
	@Override
	public void onEnable() {
		RegisterCommands();
		RegisterEvents();
		
		ArenaManager.GetInstance().LoadArenas();
	}
	
	@Override
	public void onDisable() {
		ArenaManager.GetInstance().ShutDown();
	}
	
	private void RegisterCommands() {
		ACCommand.Register(this, new ACElytraFallCommand());
	}
	
	private void RegisterEvents() {
		getServer().getPluginManager().registerEvents(new JoinSignCreate(), this);
		getServer().getPluginManager().registerEvents(new JoinSignClick(), this);
		getServer().getPluginManager().registerEvents(new JoinSignBreak(), this);
		getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
		getServer().getPluginManager().registerEvents(new PlayerLeave(), this);
		getServer().getPluginManager().registerEvents(new PlayerElytraToggle(), this);
		getServer().getPluginManager().registerEvents(new PlayerMove(), this);
		getServer().getPluginManager().registerEvents(new PlayerPortalEnter(), this);
		getServer().getPluginManager().registerEvents(new DisableCommands(), this);
		getServer().getPluginManager().registerEvents(new DisableBlockInteraction(), this);
		getServer().getPluginManager().registerEvents(new DisablePlayerDamage(), this);
		getServer().getPluginManager().registerEvents(new DisableItemDamage(), this);
		getServer().getPluginManager().registerEvents(new DisablePlayerSaturationChange(), this);
		getServer().getPluginManager().registerEvents(new DisableInventoryChange(), this);
	}
}
