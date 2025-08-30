package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.sun.jna.platform.win32.COM.util.IRawDispatchHandle;
import com.xiaohunao.heaven_destiny_moment.api.MomentManager;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationThreadManager;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.SpawnCategoryMultiplierInstance;
import com.xiaohunao.heaven_destiny_moment.common.context.SpawnCategoryMultiplierModifier;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnCategoryMultiplierInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MomentInstanceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentInstanceManager.class);

    private final Level level;
    private final MomentHistoryManager momentHistoryManager = new MomentHistoryManager();

    //时刻对应的映射表
    private final Multimap<ResourceKey<IMoment>,MomentInstance> momentMap = HashMultimap.create();
    private final Multimap<IMoment,MomentInstance> momentInstanceMap = HashMultimap.create();
    private final Multimap<MomentType<?>,MomentInstance> momentTypeMap = HashMultimap.create();

    //正在运行的时刻
    private final ConcurrentHashMap<UUID, MomentInstance> runMoments = new ConcurrentHashMap<>();

    //玩家正在参与的时刻
    private final Multimap<UUID, MomentInstance> playerMoments = HashMultimap.create();

    //客户端唯一时刻实例 //在服务端中没有作用
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

    public Collection<MomentInstance> getMomentInstances(ResourceKey<IMoment> location) {
        return momentMap.get(location);
    }

    public Collection<MomentInstance> getMomentInstances(IMoment moment) {
        return momentInstanceMap.get(moment);
    }

    public Collection<MomentInstance> getMomentInstances(MomentType<?> type) {
        return momentTypeMap.get(type);
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
                removeMomentInstance(instance);
            }
            instance.baseTick();
        }
    }


    public void addMomentInstance(MomentInstance instance) {
        runMoments.put(instance.getID(), instance);
        momentMap.put(HDMRegistries.MOMENT.getResourceKey(instance.moment).orElseThrow(), instance);
        momentInstanceMap.put(instance.moment, instance);
        momentTypeMap.put(instance.getType(), instance);
        instance.initialize();

        momentHistoryManager.addHistory(instance);

        instance.getPlayers().forEach(player -> {
            if (instance.isClientOnlyMoment() && !level.isClientSide) {
                setClientMomentInstance(player,instance);
            }
        });

        if (instance.cacheProvider.getTrackers() != null) {
            instance.cacheProvider.getTrackers().forEach(NeoForge.EVENT_BUS::register);
        }

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
        momentMap.remove(HDMRegistries.MOMENT.getResourceKey(instance.moment).orElseThrow(), instance);
        momentInstanceMap.remove(instance.moment, instance);
        momentTypeMap.remove(instance.getType(), instance);

        momentHistoryManager.finishRecord(instance);

        NeoForge.EVENT_BUS.unregister(instance.getEnemiesManager());
        NeoForge.EVENT_BUS.unregister(instance.getPlayerListManager());

        if (instance.cacheProvider.getTrackers() != null) {
            instance.cacheProvider.getTrackers().forEach(NeoForge.EVENT_BUS::unregister);
        }


        instance.getPlayers().forEach(player -> {
            if (instance.isClientOnlyMoment() && !level.isClientSide) {
                setClientMomentInstance(player,null);
            }
        });

        Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplierMap = instance.cacheProvider.getSpawnCategoryMultiplierMap();
        if (spawnCategoryMultiplierMap != null && !level.isClientSide) {
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
            instance.getMoment().momentData().flatMap(MomentData::entitySpawnSettings).ifPresent(entitySpawnSettings -> {
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

    public MomentInstance createMomentInstanceRun(MomentInstanceBuilder builder){
        MomentInstance instance = createMomentInstance(builder);
        if (instance != null && validateConditions(instance, builder)){
            addMomentInstance(instance);
        }
        return instance;
    }



    public MomentInstance createMomentInstance(MomentInstanceBuilder builder) {
        IMoment moment = builder.getMoment();
        Consumer<MomentInstance> modifier = builder.getModifier();

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
            instance.getPlayerListManager().updatePlayers();
        } catch (Exception e) {
            LOGGER.error("Failed to update players for MomentInstance", e);
        }
        return instance;
    }



    private boolean validateConditions(MomentInstance instance,MomentInstanceBuilder builder) {
        // 默认条件检查
        AutomationContext context = builder.getContext();
        boolean conditionMatch = true;
        boolean canCreate = true;

        if (builder.isCheckConditions()) {
            conditionMatch = instance.checkGeneralConditions(context);
            try {
                canCreate = instance.canCreate(context);
            } catch (Exception e) {
                LOGGER.error("Exception during canCreate check for MomentInstance", e);
                return false;
            }
        }

        // 特殊条件检查
        boolean specialConditionsPass = checkSpecialConditions(builder.getSpecialConditions(), context);

        return canCreate && conditionMatch && specialConditionsPass;
    }

    private boolean checkSpecialConditions(List<ICondition> specialConditions, AutomationContext automationContext) {
        if (specialConditions == null || specialConditions.isEmpty()) {
            return true;
        }

        for (int i = 0; i < specialConditions.size(); i++) {
            ICondition condition = specialConditions.get(i);
            if (!condition.matches(automationContext)) {
                return false;
            }
        }
        return true;
    }


    public boolean hasMoment(ResourceKey<IMoment> key) {
        return momentMap.containsKey(key);
    }




    public void addPlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.put(uuid, instance);
        if(instance.bar != null){
            instance.bar.addPlayer(player);
        }

        if (instance.isInitialized() && instance.isClientOnlyMoment() && !level.isClientSide) {
            setClientMomentInstance(player,instance);
        }
    }

    public void removePlayerToInstance(Player player, MomentInstance instance) {
        UUID uuid = player.getUUID();
        playerMoments.remove(uuid,instance);
        if(instance.bar != null){
            instance.bar.removePlayer(player);
        }


        if (instance.isInitialized() && instance.isClientOnlyMoment() && !instance.level.isClientSide){
            setClientMomentInstance(player,null);
        }
    }



    public Optional<MomentInstance> getClientMomentInstance() {
        if (!level.isClientSide){
            return Optional.empty();
        }

        if (clientOnlyMomentInstance != null && !runMoments.containsKey(clientOnlyMomentInstance.uuid)){
            this.clientOnlyMomentInstance = null;
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
            PacketDistributor.sendToPlayer((ServerPlayer) player,new ClientOnlyMomentSyncPayload(player.getUUID(),compoundTag,isRemove));
        }
    }

    public Collection<MomentInstance> getPlayerMoments(ServerPlayer player) {
        return playerMoments.get(player.getUUID());
    }

    public <T extends ITrigger> void trigger(Class<T> triggerClass, AutomationContext context) {
        final Level level = context.getLevel();

//        // 将触发器处理提交到线程池
//        AutomationThreadManager.getInstance().submitTask(() -> {
//            try {
//
//            } catch (Exception e) {
//                LOGGER.error("Error processing trigger {}", triggerClass.getSimpleName(), e);
//            }
//        });

// 获取规则（这部分可以在工作线程中执行）
        Collection<Pair<IMoment, AutomationRule>> createRules =
                MomentManager.getInstance().getRulesTriggerType(triggerClass);

        for (Pair<IMoment, AutomationRule> rulePair : createRules) {
            IMoment moment = rulePair.getFirst();
            AutomationRule rule = rulePair.getSecond();

            MomentInstanceBuilder builder = MomentInstanceBuilder.builder(moment, context);
            MomentInstance momentInstance = MomentInstanceBuilder.create(moment, context);
            AutomationContext context1 = context.toBuilder().addMomentInstance(momentInstance).build();

            if (rule.trigger().map(trigger -> trigger.canTrigger(context1)).orElse(true)) {
                IActuator actuator = rule.actuator();
                if (actuator instanceof CreateMomentInstanceActuator) {
                    // 需要在主线程执行的操作
                    final MomentInstance finalMomentInstance = momentInstance;

                    AutomationThreadManager.getInstance().addPendingTask(() -> {
                        if (validateConditions(finalMomentInstance, builder)){
                            addMomentInstance(finalMomentInstance);
                        }
                    });
                }
            }
        }

        // 处理运行中的时刻
        runMoments.values().forEach(momentInstance -> {
            AutomationContext context1 = context.toBuilder().addMomentInstance(momentInstance).build();
            momentInstance.triggerManager.trigger(triggerClass, context1);
        });
    }

}
