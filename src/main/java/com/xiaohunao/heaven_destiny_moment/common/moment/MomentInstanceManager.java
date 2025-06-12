package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
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

public class MomentInstanceManager {

    private static final TriggerTypeManager triggerTypeManager = TriggerTypeManager.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentInstanceManager.class);

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
            // 通过 Moment 对象创建一个新的 MomentInstance 实例
            instance = moment.newMomentInstance(level, moment);
            if (instance == null) {
                LOGGER.warn("Failed to create MomentInstance for moment: {}", momentKey);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while creating MomentInstance for moment: {}", momentKey, e);
            return null;
        }

        if (modifier != null) {
            try {
                modifier.accept(instance);
            } catch (Exception e) {
                LOGGER.error("Exception occurred while applying modifier to MomentInstance", e);
                // 继续执行，因为修改器失败不应该阻止实例创建
            }
        }

        try {
            // 更新实例的玩家列表
            instance.updatePlayers();
        } catch (Exception e) {
            LOGGER.error("Failed to update players for MomentInstance", e);
            // 继续执行，因为这不是致命错误
        }

        // 检查条件是否匹配
        boolean conditionMatch = checkConditions(moment, instance, pos, serverPlayer);

        // 检查实例是否可以在当前环境中创建
        boolean canCreate = false;
        try {
            canCreate = instance.canCreate(runMoments, level, pos, serverPlayer);
        } catch (Exception e) {
            LOGGER.error("Exception during canCreate check for MomentInstance", e);
            return null;
        }

        // 如果实例可以创建且条件匹配
        if (canCreate && conditionMatch) {
            try {
                // 初始化实例
                instance.init();
                // 注册实例的追踪器
                instance.registerTracker();
                // 将实例添加到管理列表中，并标记为新创建
                addMomentInstance(instance, true);

                LOGGER.debug("Successfully created MomentInstance for moment: {}", momentKey);
                return instance;
            } catch (Exception e) {
                LOGGER.error("Failed to initialize or register MomentInstance", e);
                return null;
            }
        } else {
            if (!canCreate) {
                LOGGER.debug("Cannot create MomentInstance: canCreate returned false for moment: {}", momentKey);
            }
            if (!conditionMatch) {
                LOGGER.debug("Cannot create MomentInstance: conditions not matched for moment: {}", momentKey);
            }
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

                            // 检查条件触发器
                            if (triggerContext.trigger() instanceof ConditionalTrigger conditionalTrigger) {
                                List<ICondition> conditions = conditionalTrigger.conditions();
                                if (conditions != null) {
                                    for (ICondition condition : conditions) {
                                        if (condition != null && !condition.matches(instance, pos, serverPlayer)) {
                                            LOGGER.debug("Condition not matched in ConditionalTrigger: {}", condition);
                                            return false;
                                        }
                                    }
                                }
                            }

                            // 检查创建 MomentInstance 的执行器
                            if (actuatorContext.actuator() instanceof CreateMomentInstanceActuator) {
                                List<ICondition> conditions = triggerContext.conditions();
                                if (conditions != null) {
                                    for (ICondition condition : conditions) {
                                        if (condition != null && !condition.matches(instance, pos, serverPlayer)) {
                                            LOGGER.debug("Condition not matched for CreateMomentInstanceActuator: {}", condition);
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
