package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.network.KillEntityRecorderSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListManager {
    private final UUID instanceUUID;

    protected Set<Player> players = Sets.newHashSet();

    protected Map<UUID, KillEntityRecorderAttachment> playerKillRecorders = new ConcurrentHashMap<>();

    public PlayerListManager(UUID uuid) {
        this.instanceUUID = uuid;

        NeoForge.EVENT_BUS.register(this);
    }

    public void addPlayer(Player player) {
        if (player != null) {
            this.players.add(player);
            playerKillRecorders.computeIfAbsent(player.getUUID(), uuid -> KillEntityRecorderAttachment.create());
        }
    }

    public void removePlayer(Player player) {
        if (player != null) {
            this.players.removeIf(player1 -> player.getUUID().equals(player1.getUUID()));
        }
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public KillEntityRecorderAttachment getKillRecorder(UUID uuid) {
        if (uuid == null || !playerKillRecorders.containsKey(uuid)) {
            return null;
        }
        return playerKillRecorders.get(uuid);
    }

    public void setKillRecorder(UUID uuid, KillEntityRecorderAttachment recorderAttachment) {
        if (uuid != null && recorderAttachment != null) {
            playerKillRecorders.put(uuid, recorderAttachment);
        }
    }

    public boolean hasKillRecorder(UUID uuid) {
        return uuid != null && playerKillRecorders.containsKey(uuid);
    }

    public boolean hasKillRecorder(Player player) {
        return player != null && hasKillRecorder(player.getUUID());
    }

    public boolean containsPlayer(Player player) {
        return player != null && players.contains(player);
    }

    public void mandatoryAttackRandomPlayer(Entity entity) {
        if (entity instanceof Mob mob && !this.players.isEmpty()) {
            List<Player> players = this.players.stream().filter(player -> !player.isCreative()).toList();
            Optional<Player> target = Util.getRandomSafe(players, entity.getRandom());
            target.ifPresent(player -> {
                mob.getBrain().setMemory(MemoryModuleType.ANGRY_AT, player.getUUID());
                mob.setTarget(player);
            });
        }
    }

    public Player getRandomPlayer() {
        if (players.isEmpty()) {
            return null;
        }

        List<Player> playerList = Lists.newArrayList(players);
        return Util.getRandomSafe(playerList, players.iterator().next().getRandom()).orElse(null);
    }

    @SubscribeEvent
    public void onClientPlayerNetworkLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        MomentBarOverlay.barMap.clear();
    }

//    @SubscribeEvent
//    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
//        PlayerInitSync(event.getEntity());
//    }
//
//    @SubscribeEvent
//    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
//        PlayerInitSync(event.getEntity());
//    }
//
//    private static void PlayerInitSync(Player player) {
//        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(player.level());
//        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
//            instance.updatePlayers();
//            PacketDistributor.sendToPlayer((ServerPlayer)player, new MomentManagerSyncPayload(instance.serializeNBT(),false));
//            MomentBar bar = instance.getBar();
//            if (bar != null) {
//                if(!instance.getLevel().isClientSide){
//                    bar.addBar();
//                }
//                for (Player player1 : bar.getPlayers()) {
//                    bar.addPlayer(player1);
//                }
//            }
//        }
//    }

    public void addPlayerKillCount(ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource source, Integer score) {
        if (playerKillRecorders.isEmpty() || !playerKillRecorders.containsKey(serverPlayer.getUUID())){
            return;
        }

        KillEntityRecorderAttachment killEntityRecorderAttachment = playerKillRecorders.get(serverPlayer.getUUID());
        killEntityRecorderAttachment.addKill(livingEntity, source, score);

        PacketDistributor.sendToPlayer(serverPlayer,new KillEntityRecorderSyncPayload(KillEntityRecorderAttachment.KillType.MOMENT_PLAYER,instanceUUID,killEntityRecorderAttachment));


    }
}
