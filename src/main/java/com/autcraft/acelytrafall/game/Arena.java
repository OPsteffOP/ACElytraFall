package com.autcraft.acelytrafall.game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.autcraft.acelytrafall.ACElytraFall;
import com.autcraft.acelytrafall.Path;
import com.autcraft.acelytrafall.async.AsyncTaskManager;
import com.autcraft.acelytrafall.async.AsyncTaskManager.AsyncTask;
import com.autcraft.acelytrafall.async.TemporaryCacheMap;
import com.autcraft.acelytrafall.configuration.ACConfig;
import com.autcraft.acelytrafall.configuration.ACConfigCloneable;
import com.autcraft.acelytrafall.configuration.ConfigField;
import com.autcraft.acelytrafall.game.data.PlayerGameData;
import com.autcraft.acelytrafall.game.data.PlayerGameData.PlayerConfig;
import com.autcraft.acelytrafall.game.data.PlayerGameData.RecordHologramViewType;
import com.autcraft.acelytrafall.game.data.PlayerGameState;
import com.autcraft.acelytrafall.game.data.PlayerRecordData;
import com.autcraft.acelytrafall.game.data.PlayerRecordData.TimedPlayerState;
import com.autcraft.acelytrafall.game.nms.hologram.RecordPlayerHologram;
import com.autcraft.acelytrafall.game.nms.skin.PlayerSkinRetriever;
import com.autcraft.acelytrafall.game.scoreboard.ACScoreboard;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Arena implements ACConfigCloneable<Arena> {
	
	@ConfigField public String name = null;
	@ConfigField public Location startLocation = null;
	@ConfigField public PlayerRecordData recordData = null;
	
	@ConfigField public List<Location> joinSignLocations = null;
	
	private ACConfig config = null;
	
	private PlayerSkinRetriever skinRetriever = PlayerSkinRetriever.CreatePlayerSkinRetriever();
	public Map<UUID, PlayerGameData> playerGameDataList = new HashMap<UUID, PlayerGameData>();
	
	private static final float PLAYER_UNLOAD_DELAY_SECONDS = 5*60; // time the player data will stay in memory after leaving the arena (for quicker loading)
	private TemporaryCacheMap<UUID, PlayerGameData> scheduledUnloadPlayers = new TemporaryCacheMap<UUID, PlayerGameData>(PLAYER_UNLOAD_DELAY_SECONDS);
	
	public static Arena LoadArena(String arenaName) {
		File file = new File(Path.GetArenaFilePath(arenaName.toLowerCase()));
		ACConfig config = ACConfig.loadConfiguration(file);
		
		Arena arena = (Arena)config.get("arena");
		arena.config = config;
		
		return arena;
	}
	
	public void SaveArena() {
		Arena arenaCopy = this.CreateConfigClone();
		arenaCopy.config = this.config;
		
		String taskKey = "ARENA_SAVE_" + name;
		AsyncTaskManager.GetInstance().ScheduleAsyncTask(new AsyncTask(taskKey) {
			
			@Override
			public void Execute() {
				File file = new File(Path.GetArenaFilePath(name.toLowerCase()));
				if(!file.exists()) {
					try {
						file.getParentFile().mkdirs();
						file.createNewFile();
						
						config = ACConfig.loadConfiguration(file);
						config.addDefault("arena", arenaCopy);
						config.options().copyDefaults(true);
						
						config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					config.set("arena", arenaCopy);
					
					try {
						config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void OnPlayerJoin(Player player) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
				"&aSending you to &7'%s' &a(ACElytraFall)...", name)));
		
		String taskKey = "PLAYER_" + player.getUniqueId().toString();
		AsyncTaskManager.GetInstance().ScheduleAsyncTask(new AsyncTask(taskKey) {
			
			private PlayerGameData playerGameData = null;

			@Override
			public void Execute() {
				playerGameData = LoadPlayer(player);
			}
			
			@Override
			public void OnComplete() {
				playerGameData.beforeJoinLocation = player.getLocation();
				playerGameData.beforeJoinInventoryContents = player.getInventory().getContents();
				playerGameData.beforeJoinAllowFlight = player.getAllowFlight();
				playerGameData.beforeJoinGameMode = player.getGameMode();
				playerGameData.joinTime = Integer.MIN_VALUE;
				
				playerGameData.scoreboard = new ACScoreboard("&b&lACElytraFall");
				playerGameData.scoreboard.AddStaticField("&bMap", "&7" + name);
				playerGameData.scoreboard.UpdateDynamicField("&bRecord view", "");
				playerGameData.scoreboard.AddPadding();
				playerGameData.scoreboard.UpdateDynamicField("&bTime", "");
				playerGameData.scoreboard.AddPadding();
				playerGameData.scoreboard.AddStaticField("&bTo leave", "&7/acelytrafall leave");
				playerGameData.scoreboard.AddPlayer(player);
				
				OnPlayerRecordHologramViewChange(player, playerGameData);
				
				ItemStack elytraItemStack = new ItemStack(Material.ELYTRA);
				ItemMeta elytraItemMeta = elytraItemStack.getItemMeta();
				elytraItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&l[ACElytraFall] &7Elytra"));
				List<String> elytraLore = new ArrayList<String>();
				elytraLore.add(ChatColor.translateAlternateColorCodes('&', "&5Elytra used for the ACElytraFall minigame."));
				elytraItemMeta.setLore(elytraLore);
				elytraItemStack.setItemMeta(elytraItemMeta);
				
				player.setFlying(false);
				player.setAllowFlight(false);
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();
				player.getInventory().setChestplate(elytraItemStack);
				player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				player.setFoodLevel(20);
				player.teleport(startLocation);
				
				playerGameDataList.put(player.getUniqueId(), playerGameData);
				
				UpdateJoinSignsPlayerCount();
				StartGame(player);
			}
		});
	}
	
	private void StartGame(Player player) {
		PlayerGameData playerGameData = GetPlayerGameData(player);
		playerGameData.joinTime = System.currentTimeMillis();
		
		if(playerGameData.recordPlayerHologram != null) {
			playerGameData.recordPlayerHologram.SetVisible(true);
		}
		
		playerGameData.tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(JavaPlugin.getPlugin(ACElytraFall.class), new Runnable() {

			@Override
			public void run() {
				TimedPlayerState timedPlayerState = new TimedPlayerState();
				timedPlayerState.elapsedDuration = (int)(System.currentTimeMillis() - playerGameData.joinTime);
				timedPlayerState.location = player.getLocation().clone();
				timedPlayerState.isGliding = player.isGliding();
				playerGameData.movementHistory.add(timedPlayerState);
				
				if(playerGameData.recordPlayerHologram != null) {
					playerGameData.recordPlayerHologram.ShowFrame(playerGameData, timedPlayerState.elapsedDuration);
				}
				
				float elapsedTimeSeconds = GetDurationSecondsFromMilliseconds(timedPlayerState.elapsedDuration);
				
				playerGameData.scoreboard.UpdateDynamicField("&bTime", String.format("&7%.2f", elapsedTimeSeconds));
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', 
						String.format("&aElapsed time: %.2f", elapsedTimeSeconds))));
			}
		}, 0, 1L);
	}
	
	public void OnPlayerLeave(Player player) {
		PlayerGameData playerGameData = GetPlayerGameData(player);
		playerGameData.scoreboard.RemovePlayer(player);
		playerGameData.joinTime = Integer.MIN_VALUE;
		playerGameData.movementHistory.clear();
		playerGameData.lastRecordPlayerHologramMovementHistoryIndex = 0;
		playerGameData.isFlying = false;
		
		if(playerGameData.recordPlayerHologram != null) {
			playerGameData.recordPlayerHologram.SetVisible(false);
			playerGameData.recordPlayerHologram = null;
		}
		
		Bukkit.getScheduler().cancelTask(playerGameData.tickTask);
		
		player.setFallDistance(0);
		player.getInventory().clear();
		player.setGliding(false);
		TeleportPlayer(player, playerGameData.beforeJoinLocation.clone());
		player.getInventory().setContents(playerGameData.beforeJoinInventoryContents);
		player.setAllowFlight(playerGameData.beforeJoinAllowFlight);
		player.setGameMode(playerGameData.beforeJoinGameMode);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
		
		playerGameDataList.remove(player.getUniqueId());
		scheduledUnloadPlayers.AddTemporary(player.getUniqueId(), playerGameData);
		
		UpdateJoinSignsPlayerCount();
	}
	
	public void KickAllPlayers() {
		for(UUID uuid : playerGameDataList.keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				OnPlayerLeave(player);
			}
		}
	}
	
	public boolean HasPlayerJoined(Player player) {
		return playerGameDataList.containsKey(player.getUniqueId());
	}
	
	public PlayerGameData GetPlayerGameData(Player player) {
		return GetPlayerGameData(player.getUniqueId());
	}
	
	public PlayerGameData GetPlayerGameData(UUID uuid) {
		return playerGameDataList.get(uuid);
	}
	
	public PlayerGameState GetPlayerGameState(Player player) {
		PlayerGameData playerGameData = GetPlayerGameData(player);
		return playerGameData.isFlying ? PlayerGameState.FLYING : PlayerGameState.IDLE;
	}
	
	@SuppressWarnings("incomplete-switch")
	public void UpdatePlayerGameState(Player player, PlayerGameState state) {
		switch(state) {
		case FLYING:
			GetPlayerGameData(player).isFlying = true;
			break;
		case LOSE:
			OnLoseGameState(player);
			break;
		case WIN:
			OnWinGameState(player);
			break;
		}
	}
	
	private void OnLoseGameState(Player player) {
		PlayerGameData playerGameData = GetPlayerGameData(player);
		
		Bukkit.getScheduler().cancelTask(playerGameData.tickTask);
		playerGameData.joinTime = Integer.MIN_VALUE;
		playerGameData.tickTask = -1;
		playerGameData.movementHistory.clear();
		playerGameData.lastRecordPlayerHologramMovementHistoryIndex = 0;
		playerGameData.isFlying = false;
		
		if(playerGameData.recordPlayerHologram != null) {
			playerGameData.recordPlayerHologram.SetVisible(false);
		}
		
		player.setGliding(false);
		TeleportPlayer(player, startLocation);
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
		
		StartGame(player);
	}
	
	private void OnWinGameState(Player player) {
		PlayerGameData playerGameData = GetPlayerGameData(player);
		
		if(recordData == null) {
			recordData = new PlayerRecordData();
		}
		
		if(playerGameData.personalRecordData == null) {
			playerGameData.personalRecordData = new PlayerRecordData();
		}
		
		int durationMillis = (int)(System.currentTimeMillis() - playerGameData.joinTime);
		float durationSeconds = GetDurationSecondsFromMilliseconds(durationMillis);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
				"&aYou completed map &7'%s' &awith a duration of &7%.2f seconds&a.", name, durationSeconds)));
		
		if(durationMillis < recordData.duration) {
			recordData.holder = player.getUniqueId();
			recordData.skinData = skinRetriever.RetrieveSkinData(player);
			recordData.duration = durationMillis;
			recordData.movementHistory = new ArrayList<TimedPlayerState>(playerGameData.movementHistory);
			SaveArena();
			
			OnGlobalRecordUpdate();
		}
		
		if(durationMillis < playerGameData.personalRecordData.duration) {
			playerGameData.personalRecordData.holder = player.getUniqueId();
			playerGameData.personalRecordData.skinData = skinRetriever.RetrieveSkinData(player);
			playerGameData.personalRecordData.duration = durationMillis;
			playerGameData.personalRecordData.movementHistory = new ArrayList<TimedPlayerState>(playerGameData.movementHistory);
			SavePlayer(player);
		}
		
		Bukkit.getScheduler().cancelTask(playerGameData.tickTask);
		OnPlayerLeave(player);
	}
	
	private void OnGlobalRecordUpdate() {
		for(Map.Entry<UUID, PlayerGameData> entry : playerGameDataList.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			PlayerGameData playerGameData = entry.getValue();
			
			if(playerGameData.personalConfig.recordHologramViewType == RecordHologramViewType.GLOBAL_RECORD) {
				OnPlayerRecordHologramViewChange(player, playerGameData);
			}
			
			Player recordHolder = Bukkit.getPlayer(recordData.holder);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
					"&bThe speed record of the map you're playing (&7'%s'&b) has been broken by &7'%s' &bwith a duration of &7%.2fs&b.", 
					name, recordHolder.getName(), GetDurationSecondsFromMilliseconds(recordData.duration))));
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	public void OnPlayerRecordHologramViewChange(Player player, PlayerGameData playerGameData) {
		if(playerGameData.recordPlayerHologram != null) {
			playerGameData.recordPlayerHologram.SetVisible(false);
			playerGameData.recordPlayerHologram = null;
		}
		
		switch(playerGameData.personalConfig.recordHologramViewType) {
		case PERSONAL_RECORD:
			if(playerGameData.personalRecordData != null) {
				playerGameData.recordPlayerHologram = RecordPlayerHologram.CreateHologramPlayer(playerGameData.personalRecordData, player);
			}
			break;
		case GLOBAL_RECORD:
			if(recordData != null) {
				playerGameData.recordPlayerHologram = RecordPlayerHologram.CreateHologramPlayer(recordData, player);
			}
			break;
		}
		
		playerGameData.scoreboard.UpdateDynamicField("&bRecord view", "&7" + playerGameData.personalConfig.recordHologramViewType.toString());
	}
	
	public PlayerGameData LoadPlayer(Player player) {
		return LoadPlayer(player.getUniqueId());
	}
	
	public PlayerGameData LoadPlayer(UUID uuid) {
		if(scheduledUnloadPlayers.Contains(uuid)) {
			// Player was scheduled for unload but didn't unload yet.
			// Using the same PlayerGameData to prevents having to load all the data in again.
			return scheduledUnloadPlayers.RemoveTemporary(uuid);
		}
		
		String path = Path.GetPlayerFilePath(uuid);
		File file = new File(path);
		
		if(!file.exists()) {
			PlayerGameData playerGameData = new PlayerGameData();
			playerGameData.personalConfig = new PlayerConfig();
			
			SavePlayer(uuid);
			return playerGameData;
		}
		
		ACConfig config = ACConfig.loadConfiguration(file);
		if(!config.contains(name.toLowerCase())) {
			PlayerGameData playerGameData = new PlayerGameData();
			playerGameData.personalConfig = new PlayerConfig();
			playerGameData.config = config;

			SavePlayer(uuid);
			return playerGameData;
		}
		
		PlayerGameData playerGameData = (PlayerGameData)config.get(name.toLowerCase());
		playerGameData.config = config;
		
		return playerGameData;
	}
	
	public void SavePlayer(Player player) {
		SavePlayer(player.getUniqueId());
	}
	
	public void SavePlayer(UUID uuid) {
		PlayerGameData playerGameData = GetPlayerGameData(uuid);
		if(playerGameData == null) {
			return;
		}
		
		PlayerGameData playerGameDataCopy = playerGameData.CreateConfigClone();
		playerGameDataCopy.config = playerGameData.config;
		
		String taskKey = "PLAYER_" + uuid.toString();
		AsyncTaskManager.GetInstance().ScheduleAsyncTask(new AsyncTask(taskKey) {
			
			@Override
			public void Execute() {
				String path = Path.GetPlayerFilePath(uuid);
				File file = new File(path);
				
				if(!file.exists()) {
					try {
						file.getParentFile().mkdirs();
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
					
					playerGameData.config = ACConfig.loadConfiguration(file);
					
					playerGameDataCopy.config = playerGameData.config;
					playerGameDataCopy.config.addDefault(name.toLowerCase(), playerGameDataCopy);
					playerGameDataCopy.config.options().copyDefaults(true);
					try {
						playerGameDataCopy.config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					playerGameDataCopy.config.set(name.toLowerCase(), playerGameDataCopy);
					try {
						playerGameDataCopy.config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void UpdateJoinSignsPlayerCount() {
		for(Location joinSignLocation : joinSignLocations) {
			Block block = joinSignLocation.getBlock();
			if(block != null && block.getState() != null && block.getState() instanceof Sign sign) {
				if(playerGameDataList.size() > 0) {
					sign.getSide(Side.FRONT).setLine(2, "Active Players: " + playerGameDataList.size());
				} else {
					sign.getSide(Side.FRONT).setLine(2, "");
				}
				sign.update();
			}
		}
	}
	
	private float GetDurationSecondsFromMilliseconds(int durationMillis) {
		return durationMillis / 1000.f;
	}
	
	private void TeleportPlayer(Player player, Location location) {
		// Teleports the player 1 tick later to avoid "moved to quickly" warnings
		JavaPlugin plugin = JavaPlugin.getPlugin(ACElytraFall.class);
		if(plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(ACElytraFall.class), new Runnable() {
				
				@Override
				public void run() {
					player.teleport(location);
				}
			}, 1L);
		} else {
			player.teleport(location);
		}
	}
}
