package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.mixed.ClientMomentInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MomentInstanceManager {
    private static final TriggerTypeManager triggerTypeManager = TriggerTypeManager.getInstance();

    private final Level level;

    private final ConcurrentHashMap<UUID, MomentInstance> runMoments = new ConcurrentHashMap<>();
    private final HashMultimap<ResourceLocation,MomentInstance> momentMap = HashMultimap.create();
    private final Multimap<Moment,MomentInstance> momentInstanceMap = HashMultimap.create();
    private final Multimap<UUID, MomentInstance> playerMoments = HashMultimap.create();


    public MomentInstanceManager(Level level) {
        this.level = level;
    }

    public static MomentInstanceManager of(Level level) {
        return ((MomentManagerMixed) level).heaven_destiny_moment$getMomentManager();
    }


    public CompoundTag serializeNBT() {
        CompoundTag rootTag = new CompoundTag();
        if (!runMoments.isEmpty()) {
            ListTag momentListTag = new ListTag();
            runMoments.values().forEach(momentInstance -> {
                CompoundTag momentTag = momentInstance.serializeNBT();;
                momentListTag.add(momentTag);
            });
            rootTag.put("runMoments", momentListTag);
        }
        return rootTag;
    }

    public void deserializeNBT(CompoundTag compoundTag) {
        if (compoundTag.contains("runMoments")) {
            ListTag momentListTag = compoundTag.getList("runMoments", 10);
            momentListTag.forEach(momentTag -> {
                Optional.ofNullable(MomentInstance.loadStatic(level,(CompoundTag) momentTag)).ifPresent(momentInstance -> {
                    momentInstance.registerTracker();
                    addMomentInstance(momentInstance, true);
                });

            });
        }
    }

    public MomentInstance getMomentInstance(UUID uuid) {
        return runMoments.get(uuid);
    }

    public Set<MomentInstance> getMomentInstances(ResourceLocation location) {
        return momentMap.get(location);
    }

    public Collection<MomentInstance> getMomentInstances(Moment moment) {
        return momentInstanceMap.get(moment);
    }

    public Collection<MomentInstance> getMomentInstances() {
        return runMoments.values();
    }


    public void tick() {
//        if (level.getServer() != null && level.getServer().getPlayerCount() == 0){
//            return;
//        }
        if (runMoments.isEmpty()) return;
        for (Map.Entry<UUID, MomentInstance> entry : runMoments.entrySet()) {
            MomentInstance instance = entry.getValue();
            if (instance.state == MomentState.END) {
                instance.end();
                instance.unregisterTracker();
                removeMomentInstance(instance, true);
            }
            instance.baseTick();
        }
    }

    public void addActuatorRemainingUses(MomentInstance instance){
        instance.getMoment().momentData().flatMap(MomentData::autoActuatorGroupSettings).map(AutoActuatorGroupSettings::autoActuators).ifPresent(map -> {
            map.forEach((triggerContext, actuatorContext) -> {
                triggerTypeManager.addActuatorRemainingUses(instance.getID(),actuatorContext, actuatorContext.count());
            });
        });
    }

    public void removeActuatorRemainingUses(MomentInstance instance){
        triggerTypeManager.removeActuatorRemainingUses(instance.getID());
    }


    public void addMomentInstance(MomentInstance instance, boolean isSync) {
        runMoments.put(instance.getID(), instance);
        momentMap.put(instance.getMomentResource(), instance);
        momentInstanceMap.put(instance.getMoment(), instance);
        addActuatorRemainingUses(instance);
        instance.setInitialized(true);
        instance.getPlayers().forEach(player -> {
            addPlayerAndSync(player, instance);
        });

        if (isSync && !level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBTWithoutEnemiesManager(),false));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.addPlayer(instance.getBar()));
            }
        }
    }

    public void removeMomentInstance(MomentInstance instance, boolean isSync) {
        runMoments.remove(instance.getID());
        momentMap.remove(instance.getMomentResource(), instance);
        momentInstanceMap.remove(instance.getMoment(), instance);
        removeActuatorRemainingUses(instance);
        instance.getPlayers().forEach(player -> {
            removePlayerToMoment(player, instance);
        });

        if (!level.isClientSide){
            ServerLevel serverLevel = (ServerLevel) level;
            instance.getMoment().momentData.flatMap(MomentData::entitySpawnSettings).ifPresent(entitySpawnSettings -> {
                instance.clearAllEnemiesFlags(serverLevel);
                if (entitySpawnSettings.isAfterEndClearMonster()){
                    instance.killAllEnemies(serverLevel);
                }
            });
        }



        if (isSync && !level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBTWithoutEnemiesManager(),true));
            PacketDistributor.sendToAllPlayers(new ClientOnlyMomentSyncPayload(instance.serializeNBTWithoutEnemiesManager(), true));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.removePlayer(instance.bar));
            }
        }
    }

    public MomentInstance createMomentInstance(Moment moment, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return createMomentInstance(moment, pos, serverPlayer, null);
    }

    public MomentInstance createMomentInstance(Moment moment, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, @Nullable Consumer<MomentInstance> modifier) {
        MomentInstance instance = moment.newMomentInstance(level, moment);

        Optional.ofNullable(modifier).ifPresent(consumer -> consumer.accept(instance));


        instance.updatePlayers();
        boolean conditionMatch = moment.momentData().flatMap(MomentData::autoActuatorGroupSettings)
                .map(AutoActuatorGroupSettings::autoActuators)
                .map(map -> {
                    boolean match = true;
                    for (Map.Entry<TriggerContext, ActuatorContext> entry : map.entrySet()) {
                        TriggerContext triggerContext = entry.getKey();
                        ActuatorContext actuatorContext = entry.getValue();
                        if (actuatorContext.actuator() instanceof StateSettingActuator(MomentState state) && state == MomentState.CREATE) {
                            match = triggerContext.conditions().stream().allMatch(condition -> condition.matches(instance, MomentState.CREATE, pos, serverPlayer));
                        }
                    }
                    return match;
                }).orElse(true);

        boolean canCreate = instance.canCreate(runMoments, level, pos, serverPlayer);

        if (canCreate && conditionMatch) {
            instance.init();
            instance.registerTracker();
            addMomentInstance(instance, true);
            return instance;
        }
        return null;
    }

    public boolean hasMoment(ResourceLocation key) {
        return momentMap.containsKey(key);
    }

    public boolean addPlayerToInstance(Player player, MomentInstance instance) {
        if (player == null || instance == null) {
            return false;
        }

        UUID uuid = player.getUUID();

        if (!playerMoments.containsKey(uuid)) {
            addPlayerAndSync(player, instance);
            return true;
        }

        boolean canAddPlayer = true;
        for (MomentInstance existingInstance : playerMoments.get(uuid)) {
            if (existingInstance.getID().equals(instance.getID())) {
                break;
            }

            boolean hasClientSettings = existingInstance.isClientOnlyMoment();
            if (hasClientSettings) {
                canAddPlayer = false;
                break;
            }
        }

        if (canAddPlayer) {
            addPlayerAndSync(player, instance);
            return true;
        }

        return false;
    }

    private void addPlayerAndSync(Player player, MomentInstance instance){
        if (!instance.isInitialized()){
            return;
        }

        playerMoments.put(player.getUUID(), instance);

        if (!level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBTWithoutEnemiesManager(),false));
            if (instance.isClientOnlyMoment()) {
                PacketDistributor.sendToPlayer((ServerPlayer) player, new ClientOnlyMomentSyncPayload(instance.serializeNBT(), false));
            }
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.addPlayer(instance.bar));
            }
        }


    }

    public boolean removePlayerToMoment(Player player, MomentInstance instance) {
        if (player == null || instance == null) {
            return false;
        }
        UUID uuid = player.getUUID();
        if (!playerMoments.containsKey(uuid)) {
            return false;
        }

        return playerMoments.remove(uuid, instance);
    }

    public Optional<MomentInstance> getClientMomentInstance() {
        if (!level.isClientSide){
            return Optional.empty();

        }

        return Optional.ofNullable(((ClientMomentInstanceMixed) level).heaven_destiny_moment$getClientMomentInstance());
    }

    public void setClientMomentInstance(MomentInstance momentInstance) {
        if (!level.isClientSide){
            return;
        }

        ((ClientMomentInstanceMixed) level).heaven_destiny_moment$setClientMomentInstance(momentInstance);
    }

    public Collection<MomentInstance> getPlayerMoments(ServerPlayer player) {
        return playerMoments.get(player.getUUID());
    }

}
