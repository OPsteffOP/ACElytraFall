package com.autcraft.acelytrafall.game.nms.hologram;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import com.autcraft.acelytrafall.game.data.PlayerGameData;
import com.autcraft.acelytrafall.game.data.PlayerRecordData;
import com.autcraft.acelytrafall.game.data.PlayerRecordData.TimedPlayerState;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team.CollisionRule;

@SuppressWarnings("unchecked")
public class RecordPlayerHologram_1_20_R3 extends RecordPlayerHologram {
	
	private static EntityDataAccessor<Byte> ENTITY_DATA_ACCESSOR = null;
	private static org.bukkit.inventory.ItemStack ELYTRA_ITEMSTACK = null;
	
	private ServerPlayer nmsPlayer = null;
	private PlayerTeam team = null;
	
	static {
		for(Field field : Entity.class.getDeclaredFields()) {
			if(field.getGenericType() instanceof ParameterizedType fieldType) {
				if(fieldType.getRawType() == EntityDataAccessor.class && 
						fieldType.getActualTypeArguments().length == 1 && fieldType.getActualTypeArguments()[0] == Byte.class) {
					field.setAccessible(true);
					try {
						ENTITY_DATA_ACCESSOR = (EntityDataAccessor<Byte>)field.get(null);
						break;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if(ENTITY_DATA_ACCESSOR == null) {
			Bukkit.getLogger().log(Level.SEVERE, String.format("%s - Couldn't find Entity.DATA_SHARED_FLAGS_ID field.", RecordPlayerHologram_1_20_R3.class.getSimpleName()));
		}
		
		ELYTRA_ITEMSTACK = new org.bukkit.inventory.ItemStack(Material.ELYTRA);
	}
	
	public RecordPlayerHologram_1_20_R3(PlayerRecordData data, Player player) {
		super(data, player);
		Spawn();
	}
	
	@Override
	public void Spawn() {
		MinecraftServer nmsServer = ((CraftServer)Bukkit.getServer()).getServer();
		ServerLevel nmsWorld = ((CraftWorld)player.getWorld()).getHandle();
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), Bukkit.getOfflinePlayer(data.holder).getName());
		gameProfile.getProperties().put("textures", new Property("textures", data.skinData.texture, data.skinData.signature));
		nmsPlayer = new ServerPlayer(nmsServer, nmsWorld, gameProfile, ClientInformation.createDefault());
		SetVisible(true);
	}
	
	@Override
	public void SetVisible(boolean isVisible) {
		if(this.isVisible == isVisible) {
			return;
		}
		
		if(isVisible) {
			GameProfile gameProfile = nmsPlayer.getGameProfile();
			
			EnumSet<Action> actions = EnumSet.noneOf(Action.class);
			actions.add(Action.ADD_PLAYER);
			actions.add(Action.UPDATE_DISPLAY_NAME);
			actions.add(Action.UPDATE_LISTED);
			
			ByteBuf directBuffer = Unpooled.directBuffer();
			FriendlyByteBuf buffer = new FriendlyByteBuf(directBuffer);
			buffer.writeEnumSet(actions, ClientboundPlayerInfoUpdatePacket.Action.class);
			buffer.writeVarInt(1); // nmsPlayer count
			buffer.writeUUID(nmsPlayer.getUUID()); // nmsPlayer uuid
			// data for ADD_PLAYER action
			buffer.writeUtf(gameProfile.getName(), 16);
			buffer.writeGameProfileProperties(gameProfile.getProperties());
			// data for UPDATE_DISPLAY_NAME action
			buffer.writeNullable(nmsPlayer.getTabListDisplayName(), FriendlyByteBuf::writeComponent);
			// data for UPDATE_LISTED action
			buffer.writeBoolean(false); // don't list player on tab
			
			List<DataValue<?>> entityData = new ArrayList<DataValue<?>>();
			entityData.add(DataValue.create(ENTITY_DATA_ACCESSOR, Byte.valueOf((byte)0x20))); // sets the player invisible (0x20) - https://wiki.vg/Entity_metadata#Entity
			
			List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<Pair<EquipmentSlot, ItemStack>>();
			equipment.add(new Pair<EquipmentSlot, ItemStack>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(ELYTRA_ITEMSTACK)));
			
			Scoreboard scoreboard = new Scoreboard(); // needed to show the nmsPlayer as a ghost (because of invisibility flag above)
			team = scoreboard.addPlayerTeam(player.getName());
			team.setSeeFriendlyInvisibles(true);
			team.setCollisionRule(CollisionRule.NEVER);
			scoreboard.addPlayerToTeam(player.getName(), team);
			scoreboard.addPlayerToTeam(gameProfile.getName(), team);
			
			ServerGamePacketListenerImpl connection = ((CraftPlayer)player).getHandle().connection;
			connection.send(new ClientboundPlayerInfoUpdatePacket(buffer));
			connection.send(new ClientboundAddEntityPacket(nmsPlayer));
			connection.send(new ClientboundTeleportEntityPacket(nmsPlayer));
			connection.send(new ClientboundSetEntityDataPacket(nmsPlayer.getId(), entityData));
			connection.send(new ClientboundSetEquipmentPacket(nmsPlayer.getId(), equipment));
			connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
		} else {
			ServerGamePacketListenerImpl connection = ((CraftPlayer)player).getHandle().connection;
			connection.send(new ClientboundPlayerInfoRemovePacket(Arrays.asList(nmsPlayer.getGameProfile().getId())));
			connection.send(new ClientboundRemoveEntitiesPacket(nmsPlayer.getId()));
			connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(team));
		}
		
		this.isVisible = isVisible;
	}
	
	@Override
	public void ShowFrame(PlayerGameData playerGameData, int elapsedDuration) {
		if(data.movementHistory.isEmpty()) {
			return;
		}
		
		int startIndex = 0;
		TimedPlayerState lastFrameData = data.movementHistory.get(playerGameData.lastRecordPlayerHologramMovementHistoryIndex);
		if(lastFrameData != null && elapsedDuration >= lastFrameData.elapsedDuration) {
			startIndex = playerGameData.lastRecordPlayerHologramMovementHistoryIndex;
		} else {
			startIndex = 0;
		}
		
		TimedPlayerState frameData = null;
		int lastElapsedDuration = Integer.MAX_VALUE;
		for(int i = startIndex; i < data.movementHistory.size(); ++i) {
			frameData = data.movementHistory.get(i);
			if(frameData.elapsedDuration >= elapsedDuration) {
				if(lastElapsedDuration != Integer.MAX_VALUE && 
						Math.abs(lastElapsedDuration - elapsedDuration) < Math.abs(frameData.elapsedDuration - elapsedDuration)) {
					// Previous index 'i - 1' is the closest frame data to the elapsedDuration argument
					frameData = data.movementHistory.get(i - 1);
				}
				break;
			}
			
			lastElapsedDuration = frameData.elapsedDuration;
		}
		
		nmsPlayer.setPos(frameData.location.getX(), frameData.location.getY(), frameData.location.getZ());
		nmsPlayer.setXRot(frameData.location.getPitch());
		nmsPlayer.setYRot(frameData.location.getYaw());
		
		if(!isVisible) {
			SetVisible(true);
		}
		
		byte entityDataValue = (byte)0x20; // sets the player invisible - https://wiki.vg/Entity_metadata#Entity
		if(frameData.isGliding) {
			entityDataValue |= (byte)0x80; // sets the player gliding - https://wiki.vg/Entity_metadata#Entity
		}
		List<DataValue<?>> entityData = new ArrayList<DataValue<?>>();
		entityData.add(DataValue.create(ENTITY_DATA_ACCESSOR, Byte.valueOf(entityDataValue)));
		
		ServerGamePacketListenerImpl connection = ((CraftPlayer)player).getHandle().connection;
		connection.send(new ClientboundTeleportEntityPacket(nmsPlayer));
		connection.send(new ClientboundSetEntityDataPacket(nmsPlayer.getId(), entityData));
	}
}
