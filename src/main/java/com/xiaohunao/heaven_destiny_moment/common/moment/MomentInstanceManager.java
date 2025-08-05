package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.SpawnCategoryMultiplierInstance;
import com.xiaohunao.heaven_destiny_moment.common.context.SpawnCategoryMultiplierModifier;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnCategoryMultiplierInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    private final Multimap<ResourceKey<Moment>,MomentInstance> momentMap = HashMultimap.create();
    private final Multimap<Moment,MomentInstance> momentInstanceMap = HashMultimap.create();

    //客户端唯一时刻实例 //在服务端中没有作用,只是标记
    public MomentInstance clientOnlyMomentInstance = null;


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

    public Collection<MomentInstance> getMomentInstances(ResourceKey<Moment> location) {
        return momentMap.get(location);
    }

    public Collection<MomentInstance> getMomentInstances(Moment moment) {
        return momentInstanceMap.get(moment);
    }

    public Collection<MomentInstance> getMomentInstances() {
        return runMoments.values();
    }

    public ConcurrentHashMap<UUID, MomentInstance> getRunMoments() {
        return runMoments;
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
        momentMap.put(HDMRegistries.MOMENT.getResourceKey(instance.moment).orElseThrow(), instance);
        momentInstanceMap.put(instance.moment, instance);

        addActuatorRemainingUses(instance);
        momentHistoryManager.addHistory(instance);

        instance.cacheProvider.iniCache();

        Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplierMap = instance.cacheProvider.getSpawnCategoryMultiplierMap();
        if (spawnCategoryMultiplierMap != null && !level.isClientSide) {
            spawnCategoryMultiplierMap.forEach((mobCategory, multiplierModifier) -> {
                SpawnCategoryMultiplierInstanceMixed spawnCategoryMultiplierInstanceMixed = (SpawnCategoryMultiplierInstanceMixed) level;
                SpawnCategoryMultiplierInstance multiplierInstance = spawnCategoryMultiplierInstanceMixed.hdm$getMobCategoryMultiplierInstance(mobCategory);
                if (multiplierInstance != null) {
                    multiplierInstance.addModifier(multiplierModifier);
                } else {
                    LOGGER.warn("SpawnCategoryMultiplierInstance for {} is null in level {}", mobCategory, level);
                }
            });
        }

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
        momentInstanceMap.remove(instance.moment, instance);
        removeActuatorRemainingUses(instance);

        momentHistoryManager.finishRecord(instance);

        Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplierMap = instance.cacheProvider.getSpawnCategoryMultiplierMap();
        if (spawnCategoryMultiplierMap != null) {
            spawnCategoryMultiplierMap.forEach((mobCategory, multiplierModifier) -> {
                SpawnCategoryMultiplierInstanceMixed spawnCategoryMultiplierInstanceMixed = (SpawnCategoryMultiplierInstanceMixed) level;
                SpawnCategoryMultiplierInstance multiplierInstance = spawnCategoryMultiplierInstanceMixed.hdm$getMobCategoryMultiplierInstance(mobCategory);
                if (multiplierInstance != null) {
                    multiplierInstance.removeModifier(multiplierModifier);
                } else {
                    LOGGER.warn("SpawnCategoryMultiplierInstance for {} is null in level {}", mobCategory, level);
                }
            });
        }

        instance.getPlayers().forEach(player -> {
            removePlayerToInstance(player, instance);
        });

        if (!level.isClientSide){
            ServerLevel serverLevel = (ServerLevel) level;
            instance.getMoment().momentData.flatMap(MomentData::entitySpawnSettings).ifPresent(entitySpawnSettings -> {
                if (entitySpawnSettings.isAfterEndClearMonster()){
                    instance.killAllEnemies(serverLevel);
                }
            });
        }



        if (!level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),true));
            if (instance.bar != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.removeBar(instance.bar));
            }
        }
    }

    public MomentInstance createMomentInstance(MomentInstanceBuilder builder) {
        Moment moment = builder.getMoment();
        Level level = builder.getLevel();
        BlockPos pos = builder.getPos();
        ServerPlayer serverPlayer = builder.getServerPlayer();
        Consumer<MomentInstance> modifier = builder.getModifier();
        boolean isCheckConditions = builder.isCheckConditions();
        List<ICondition> specialConditions = builder.getSpecialConditions();

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
            instance.cacheProvider.iniCache();
        } catch (Exception e) {
            LOGGER.error("Exception occurred while creating MomentInstance for moment: {}", momentKey, e);
            return null;
        }

        try {
            instance.updatePlayers();
        } catch (Exception e) {
            LOGGER.error("Failed to update players for MomentInstance", e);
        }

        // 条件验证
        if (!validateConditions(instance, pos, serverPlayer, isCheckConditions, specialConditions, momentKey)) {
            return null;
        }

        // 完成创建
        try {
            instance.registerTracker();
            instance.initialize();
            addMomentInstance(instance);
            return instance;
        } catch (Exception e) {
            LOGGER.error("Failed to initialize or register MomentInstance", e);
            return null;
        }
    }


    private boolean validateConditions(MomentInstance instance,
                                       BlockPos pos, ServerPlayer serverPlayer,
                                       boolean isCheckConditions, List<ICondition> specialConditions,
                                       ResourceLocation momentKey) {
        // 默认条件检查
        boolean conditionMatch = true;
        boolean canCreate = true;

        if (isCheckConditions) {
            conditionMatch = instance.checkGeneralConditions(pos, serverPlayer);
            try {
                canCreate = instance.canCreate(getRunMoments(), level, pos, serverPlayer);
            } catch (Exception e) {
                LOGGER.error("Exception during canCreate check for MomentInstance", e);
                return false;
            }
        }

        // 特殊条件检查
        boolean specialConditionsPass = checkSpecialConditions(specialConditions, instance, pos, serverPlayer, momentKey);

        return canCreate && conditionMatch && specialConditionsPass;
    }

    private boolean checkSpecialConditions(List<ICondition> specialConditions, MomentInstance instance,
                                           BlockPos pos, ServerPlayer serverPlayer, ResourceLocation momentKey) {
        if (specialConditions == null || specialConditions.isEmpty()) {
            return true;
        }

        for (int i = 0; i < specialConditions.size(); i++) {
            ICondition condition = specialConditions.get(i);
            try {
                if (!condition.matches(instance, pos, serverPlayer)) {
                    LOGGER.debug("Special condition {} failed at index {} for moment: {}",
                            condition.getClass().getSimpleName(), i, momentKey);
                    return false;
                }
            } catch (Exception e) {
                LOGGER.error("Exception while checking special condition at index {} for moment: {}: {}",
                        i, momentKey, condition.getClass().getSimpleName(), e);
                return false;
            }
        }
        return true;
    }


    public boolean hasMoment(ResourceKey<Moment> key) {
        return momentMap.containsKey(key);
    }

    public void addPlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.put(uuid, instance);
        if(instance.bar != null){
            instance.bar.addPlayer(player);
        }

        if (instance.isClientOnlyMoment()){
            setClientMomentInstance(player,instance);
        }
    }

    public void removePlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.remove(uuid,instance);
        if(instance.bar != null){
            instance.bar.removePlayer(player);
        }


        if (instance.isClientOnlyMoment()){
            setClientMomentInstance(player,null);
        }
    }



    public Optional<MomentInstance> getClientMomentInstance() {
        if (!level.isClientSide){
            return Optional.empty();
        }
        return Optional.ofNullable(this.clientOnlyMomentInstance);
    }

    public void setClientMomentInstance(Player player,MomentInstance momentInstance) {
        if (level.isClientSide){
            this.clientOnlyMomentInstance = momentInstance;
        }

        if (!level.isClientSide){
            CompoundTag compoundTag = ClientOnlyMomentSyncPayload.CLEAR_ALL_TAG;
            boolean isRemove = true;
            if (momentInstance != null) {
                compoundTag = momentInstance.serializeNBT();
                isRemove = false;
            }
            PacketDistributor.sendToPlayer((ServerPlayer) player,new ClientOnlyMomentSyncPayload(compoundTag,isRemove));
        }
    }

    public Collection<MomentInstance> getPlayerMoments(ServerPlayer player) {
        return playerMoments.get(player.getUUID());
    }

}
