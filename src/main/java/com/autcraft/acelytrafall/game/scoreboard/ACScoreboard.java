package com.autcraft.acelytrafall.game.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class ACScoreboard {
	
	private Scoreboard scoreboard = null;
	private Objective objective = null;
	
	private short nextUniqueEntryIndex = 0;
	
	public ACScoreboard(String name) {
		String coloredName = ChatColor.translateAlternateColorCodes('&', name);
		String key = ChatColor.stripColor(coloredName).toLowerCase();
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective(key, Criteria.DUMMY, coloredName);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void AddPadding() {
		String entry = GetUniqueInvisibleEntry();
		AddStaticField(entry, "");
	}
	
	public void AddStaticField(String name, String value) {
		if(!ChatColor.stripColor(name).isEmpty()) {
			name = name + ": ";
		}
		
		IncrementScores();
		objective.getScore(ChatColor.translateAlternateColorCodes('&', String.format("%s%s", name, value))).setScore(0);
	}
	
	public void UpdateDynamicField(String name, String value) {
		String key = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name)).toLowerCase();
		String entry = GetUniqueInvisibleEntry();
		
		if(!ChatColor.stripColor(name).isEmpty()) {
			name = name + ": ";
		}
		
		Team team = scoreboard.getTeam(key);
		if(team == null) {
			IncrementScores();
			team = scoreboard.registerNewTeam(key);
			team.addEntry(entry);
			objective.getScore(entry).setScore(0);
		}
		
		team.setPrefix(ChatColor.translateAlternateColorCodes('&', name));
		team.setSuffix(ChatColor.translateAlternateColorCodes('&', value));
	}
	
	public void AddPlayer(Player player) {
		player.setScoreboard(scoreboard);
	}
	
	public void RemovePlayer(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}
	
	private void IncrementScores() {
		for(String entry : scoreboard.getEntries()) {
			Score score = objective.getScore(entry);
			if(score.isScoreSet()) {
				score.setScore(score.getScore() + 1);
			}
		}
	}
	
	private static final String COLOR_CODE_OPTIONS = "0123456789abcdeflnokmr";
	private String GetUniqueInvisibleEntry() {
		if(nextUniqueEntryIndex >= COLOR_CODE_OPTIONS.length()) {
			return null;
		}
		
		String uniqueEntry = String.valueOf(ChatColor.COLOR_CHAR) + COLOR_CODE_OPTIONS.charAt(nextUniqueEntryIndex);
		++nextUniqueEntryIndex;
		
		return uniqueEntry;
	}
}
