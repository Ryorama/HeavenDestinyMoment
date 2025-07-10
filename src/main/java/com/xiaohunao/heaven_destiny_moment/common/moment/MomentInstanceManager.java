package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.mixed.ClientMomentInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.ConditionalTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class MomentInstanceManager {

    private static final TriggerTypeManager triggerTypeManager = TriggerTypeManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentInstanceManager.class);

    private final Level level;
    private final MomentHistoryManager momentHistoryManager = new MomentHistoryManager();

    //正在运行的时刻
    private final ConcurrentHashMap<UUID, MomentInstance> runMoments = new ConcurrentHashMap<>();

    //玩家正在参与的时刻
    private final Multimap<UUID, MomentInstance> playerMoments = HashMultimap.create();

    //时刻对应的映射表
    private final HashMultimap<ResourceLocation,MomentInstance> momentMap = HashMultimap.create();
    private final Multimap<Moment,MomentInstance> momentInstanceMap = HashMultimap.create();


    public MomentInstanceManager(Level level) {
        this.level = level;
    }

    public static MomentInstanceManager of(Level level) {
        return ((MomentManagerMixed) level).heaven_destiny_moment$getMomentManager();
    }

    public MomentHistoryManager getMomentHistoryManager() {
        return momentHistoryManager;
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
        ListTag historyTag = momentHistoryManager.serializeNBT();
        if (!historyTag.isEmpty()) {
            rootTag.put("history", historyTag);
        }

        return rootTag;
    }

    public void deserializeNBT(CompoundTag compoundTag) {
        if (compoundTag.contains("runMoments")) {
            ListTag momentListTag = compoundTag.getList("runMoments", Tag.TAG_COMPOUND);
            momentListTag.forEach(momentTag -> {
                MomentInstance momentInstance = MomentInstance.loadStatic(level, (CompoundTag) momentTag);
                if (momentInstance != null) {
                    momentInstance.registerTracker();
                    addMomentInstance(momentInstance);
                }
            });
        }

        if (compoundTag.contains("history")) {
            momentHistoryManager.deserializeNBT(compoundTag.getList("history", Tag.TAG_COMPOUND));
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
        if (runMoments.isEmpty()) return;
        for (Map.Entry<UUID, MomentInstance> entry : runMoments.entrySet()) {
            MomentInstance instance = entry.getValue();

            if (instance.state == MomentState.END) {
                instance.end();
                instance.unregisterTracker();
                removeMomentInstance(instance);
            }
            instance.baseTick();
        }
    }

    public void addActuatorRemainingUses(MomentInstance instance){
        instance.getMoment()
                .momentData()
                .flatMap(MomentData::autoActuatorGroupSettings)
                .map(AutoActuatorGroupSettings::autoActuators)
                .ifPresent(map -> {
                    map.forEach((triggerContext, actuatorContext) -> {
                        triggerTypeManager.addActuatorRemainingUses(instance.getID(),actuatorContext, actuatorContext.count());
                    });
                });
    }

    public void removeActuatorRemainingUses(MomentInstance instance){
        triggerTypeManager.removeActuatorRemainingUses(instance.getID());
    }


    public void addMomentInstance(MomentInstance instance) {
        runMoments.put(instance.getID(), instance);
        momentMap.put(instance.getMomentResource(), instance);
        momentInstanceMap.put(instance.getMoment(), instance);

        addActuatorRemainingUses(instance);
        momentHistoryManager.addHistory(instance);

        if (!level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),false));
            if (instance.getBar() != null) {
                instance.getBar().addBar();
            }
        }
    }

    public void removeMomentInstance(MomentInstance instance) {
        runMoments.remove(instance.getID());
        momentMap.remove(instance.getMomentResource(), instance);
        momentInstanceMap.remove(instance.getMoment(), instance);
        removeActuatorRemainingUses(instance);

        momentHistoryManager.finishRecord(instance);

        instance.getPlayers().forEach(player -> {
            removePlayerToInstance(player, instance);
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



        if (!level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),true));
            PacketDistributor.sendToAllPlayers(new ClientOnlyMomentSyncPayload(instance.serializeNBT(), true));
            if (instance.bar != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.removeBar(instance.bar));
            }
        }
    }

    public MomentInstance createMomentInstance(Moment moment, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return createMomentInstance(moment, pos, serverPlayer, null);
    }

    public MomentInstance createMomentInstance(Moment moment, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, @Nullable Consumer<MomentInstance> modifier) {
        if (moment == null) {
            LOGGER.error("Attempted to create MomentInstance with null Moment");
            throw new IllegalArgumentException("Moment cannot be null");
        }

        if (level == null) {
            LOGGER.error("Cannot create MomentInstance: level is null");
            return null;
        }
        ResourceLocation momentKey = HDMRegistries.MOMENT.getKey(moment);
        MomentInstance instance;
        try {
            instance = moment.newMomentInstance(level, moment);
            if (instance == null) {
                LOGGER.warn("Failed to create MomentInstance for moment: {}", momentKey);
                return null;
            }

            if (modifier != null) {
                try {
                    modifier.accept(instance);
                } catch (Exception e) {
                    LOGGER.error("Exception occurred while applying modifier to MomentInstance", e);
                }
            }

            instance.init();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while creating MomentInstance for moment: {}", momentKey, e);
            return null;
        }



        try {
            instance.updatePlayers();
        } catch (Exception e) {
            LOGGER.error("Failed to update players for MomentInstance", e);
        }

        boolean conditionMatch = checkConditions(moment, instance, pos, serverPlayer);
        boolean canCreate;
        try {
            canCreate = instance.canCreate(runMoments, level, pos, serverPlayer);
        } catch (Exception e) {
            LOGGER.error("Exception during canCreate check for MomentInstance", e);
            return null;
        }

        if (canCreate && conditionMatch) {
            try {
                instance.registerTracker();
                addMomentInstance(instance);
                return instance;
            } catch (Exception e) {
                LOGGER.error("Failed to initialize or register MomentInstance", e);
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean checkConditions(Moment moment, MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        try {
            return moment.momentData()
                    .flatMap(MomentData::autoActuatorGroupSettings)
                    .map(AutoActuatorGroupSettings::autoActuators)
                    .map(map -> {
                        for (Map.Entry<TriggerContext, ActuatorContext> entry : map.entrySet()) {
                            TriggerContext triggerContext = entry.getKey();
                            ActuatorContext actuatorContext = entry.getValue();

                            if (triggerContext.trigger() instanceof ConditionalTrigger conditionalTrigger) {
                                List<ICondition> conditions = conditionalTrigger.conditions();
                                if (conditions != null) {
                                    for (ICondition condition : conditions) {
                                        if (condition != null && !condition.matches(instance, pos, serverPlayer)) {
                                            return false;
                                        }
                                    }
                                }
                            }

                            if (actuatorContext.actuator() instanceof CreateMomentInstanceActuator) {
                                List<ICondition> conditions = triggerContext.conditions();
                                if (conditions != null) {
                                    for (ICondition condition : conditions) {
                                        if (condition != null && !condition.matches(instance, pos, serverPlayer)) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    }).orElse(true);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while checking conditions for MomentInstance", e);
            return false;
        }
    }

    public boolean hasMoment(ResourceLocation key) {
        return momentMap.containsKey(key);
    }

    public void addPlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.put(uuid, instance);
        if(instance.bar != null){
            instance.bar.addPlayer(player);
        }

    }

    public void removePlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.remove(uuid,instance);
        if(instance.bar != null){
            instance.bar.removePlayer(player);
        }
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
