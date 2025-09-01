package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntityTypeScoreTable;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.event.MomentEvent;
import com.xiaohunao.heaven_destiny_moment.common.event.PlayerMomentAreaEvent;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.network.KillEntityRecorderSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.KillRequiredSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentStateSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.OpenAreaSpawnAlgorithm;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public abstract class MomentInstance extends AttachmentHolder {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final MomentInstanceManager momentInstanceManager;
    protected final Level level;
    protected final MomentType<?> type;
    protected final UUID uuid;
    protected final IMoment moment;

    private boolean initialized = false;

    protected MomentBar bar;
    protected long tick = -1L;
    protected EnemiesManager enemiesManager;
    protected PlayerListManager playerListManager;
    protected TriggerManager triggerManager;
    protected MomentInstanceCacheProvider cacheProvider;

    protected MomentState state = MomentState.UNINITIALIZED;

    protected Set<UUID> inAreaPlayers = Sets.newHashSet();
    protected Set<Vec3> spawnPosList = Sets.newHashSet();
    protected CompoundTag persistentData = new CompoundTag();
    protected Map<ResourceLocation, Pair<KillEntityCondition,KillEntityCondition.RequiredKill>> tryRequiredKill = new HashMap<>();

    protected MomentInstance(MomentType<?> type, Level level, IMoment moment) {
        this(type, UUID.randomUUID(), level, moment);
    }

    protected MomentInstance(MomentType<?> type, UUID uuid, Level level, IMoment moment) {
        this.uuid = uuid;
        this.type = type;
        this.level = level;
        this.moment = moment;
        this.enemiesManager = new EnemiesManager(this);
        this.playerListManager = new PlayerListManager(this);
        this.cacheProvider = new MomentInstanceCacheProvider(moment);
        this.triggerManager = new TriggerManager(this);
        this.momentInstanceManager = null;
    }


    public void eventBusRegister(){
        List<Object> listeners = Lists.newArrayList(enemiesManager,playerListManager);
        listeners.addAll(cacheProvider.getTrackers() != null ? cacheProvider.getTrackers() : Lists.newArrayList());
        listeners.forEach(NeoForge.EVENT_BUS::register);
    }

    public void eventBusUnregister(){
        List<Object> listeners = Lists.newArrayList(enemiesManager,playerListManager);
        listeners.addAll(cacheProvider.getTrackers() != null ? cacheProvider.getTrackers() : Lists.newArrayList());
        listeners.forEach(NeoForge.EVENT_BUS::unregister);
    }




    protected MomentInstanceManager getMomentManager() {
        if (this.momentInstanceManager == null) {
            return MomentInstanceManager.of(level);
        }
        return this.momentInstanceManager;
    }

    public IMoment getMoment() {
        return moment;
    }

    public MomentInstanceCacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void init() {
        initMomentBar();
        initSpawnPosList();
        initTryModifyStateRequiredKill();
    }


    public void initMomentBar() {
        moment.barRenderType().ifPresent(barRenderType -> this.bar = new MomentBar(uuid, barRenderType));
    }

    public void initSpawnPosList() {

    }

    public void initTryModifyStateRequiredKill() {
        cacheProvider.getAutoActuatorGroupSettings().createRule().ifPresent(rule -> {
            rule.conditions().ifPresent(conditions -> {
                conditions.forEach(condition -> {
                    if (condition instanceof KillEntityCondition killEntityCondition) {
                        KillEntityCondition.RequiredKill killRecord = killEntityCondition.getKillRecord(this);
                        tryRequiredKill.put(rule.name(), new Pair<>(killEntityCondition, killRecord));
                    }
                });
            });
        });

        cacheProvider.getAutoActuatorGroupSettings().runtimeRules().ifPresent(rules -> {
            rules.forEach(rule -> rule.conditions().ifPresent(conditions -> {
                conditions.forEach(condition -> {
                    if (condition instanceof KillEntityCondition killEntityCondition) {
                        KillEntityCondition.RequiredKill killRecord = killEntityCondition.getKillRecord(this);
                        tryRequiredKill.put(rule.name(), new Pair<>(killEntityCondition, killRecord));
                    }
                });
            }));
        });
    }

    public long getTick() {
        return tick;
    }

    public EnemiesManager getEnemiesManager() {
        return enemiesManager;
    }

    public PlayerListManager getPlayerListManager() {
        return playerListManager;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public Vec3 getRandomSpawnPos() {
        if (spawnPosList.isEmpty()) {
            return Vec3.ZERO;
        }

        List<Vec3> vec3s = Lists.newArrayList(spawnPosList);
        return vec3s.get(level.random.nextInt(spawnPosList.size()));
    }

    public void updateBarProgress(float progress) {
        progress = Mth.clamp(progress, 0.0f, 1.0f);
        if (this.bar != null) {
            this.bar.updateProgress(level,progress);
        }
    }

    @Nullable
    public static MomentInstance loadStatic(Level level, CompoundTag compoundTag) {
        String id = compoundTag.getString("id");
        ResourceLocation resourcelocation = ResourceLocation.tryParse(id);
        if (resourcelocation == null) {
            LOGGER.error("MomentInstance has invalid type: {}", id);
            return null;
        } else {
            return HDMRegistries.MOMENT_TYPE.getOptional(resourcelocation).map(momentType -> {
                try {
                    Tag tag = compoundTag.get("moment");
                    DataResult<Pair<IMoment, Tag>> decode = HDMRegistries.MOMENT.byNameCodec().decode(NbtOps.INSTANCE, tag);
                    if (decode.isSuccess()) {
                        IMoment moment = decode.getOrThrow().getFirst();
                        return momentType.create(compoundTag.getUUID("uuid"), level, moment);
                    } else {
                        LOGGER.error("MomentInstance has invalid moment data: {}", decode.getOrThrow());
                        return null;
                    }
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to create MomentInstance {}", id, throwable);
                    return null;
                }
            }).map(momentType -> {
                try {
                    momentType.deserializeNBT(compoundTag);
                    return momentType;
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to load data for MomentInstance {}", id, throwable);
                    return null;
                }
            }).orElseGet(() -> {
                LOGGER.warn("Skipping MomentInstance with id {}", id);
                return null;
            });
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();

        serializeMetaData(compoundTag);
        CompoundTag attachments = serializeAttachments(level.registryAccess());
        if (attachments != null) {
            compoundTag.put(ATTACHMENTS_NBT_KEY, attachments);
        }

        compoundTag.put("enemies_manager", enemiesManager.serializeNBT());
        compoundTag.put("persistentData", this.persistentData);
        compoundTag.putLong("tick", tick);
        if (this.bar != null) {
            compoundTag.put("bar", MomentBar.CODEC.encodeStart(NbtOps.INSTANCE, this.bar).getOrThrow());
        }

        if (state != null) {
            compoundTag.putString("state", state.name());
        }

        ListTag spawnPosListTag = new ListTag();
        spawnPosList.forEach(vec3 -> spawnPosListTag.add(Vec3.CODEC.encodeStart(NbtOps.INSTANCE, vec3).getOrThrow()));
        compoundTag.put("spawnPosList", spawnPosListTag);

        if (tryRequiredKill != null) {
            Tag tag = Codec.unboundedMap(ResourceLocation.CODEC, Codec.pair(KillEntityCondition.CODEC.codec(), KillEntityCondition.RequiredKill.CODEC))
                    .encodeStart(NbtOps.INSTANCE, tryRequiredKill).getOrThrow();
            compoundTag.put("tryRequiredKill", tag);
        }
        compoundTag.put("trigger_manager", triggerManager.serializeNBT());

        return compoundTag;
    }

    public void deserializeNBT(CompoundTag compoundTag) {
        if (compoundTag.contains(ATTACHMENTS_NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            deserializeAttachments(level.registryAccess(), compoundTag.getCompound(ATTACHMENTS_NBT_KEY));
        }
        enemiesManager.deserializeNBT(compoundTag.getCompound("enemies_manager"));
        this.persistentData = compoundTag.getCompound("persistentData");
        this.tick = compoundTag.getLong("tick");

        if (compoundTag.contains("bar")) {
            this.bar = MomentBar.CODEC.decode(NbtOps.INSTANCE, compoundTag.get("bar")).getOrThrow().getFirst();
        }

        if (compoundTag.contains("state")) {
            this.state = MomentState.valueOf(compoundTag.getString("state"));
        }

        ListTag spawnPosListTag = compoundTag.getList("spawnPosList", Tag.TAG_LIST);
        spawnPosListTag.forEach(tag -> spawnPosList.add(Vec3.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst()));

        if (compoundTag.contains("tryRequiredKill")) {
            this.tryRequiredKill = Codec.unboundedMap(ResourceLocation.CODEC, Codec.pair(KillEntityCondition.CODEC.codec(), KillEntityCondition.RequiredKill.CODEC))
                    .decode(NbtOps.INSTANCE, compoundTag.get("tryRequiredKill")).getOrThrow().getFirst();
        }

        if (compoundTag.contains("trigger_manager")) {
            this.triggerManager.deserializeNBT(compoundTag.getList("trigger_manager", Tag.TAG_COMPOUND));
        }
    }

    private void serializeMetaData(CompoundTag compoundTag) {
        compoundTag.putUUID("uuid", uuid);
        compoundTag.putString("id", getRegistryName().toString());
        compoundTag.put("moment", ResourceKey.codec(HDMRegistries.Keys.MOMENT).encodeStart(NbtOps.INSTANCE, HDMRegistries.MOMENT.getResourceKey(moment).get()).getOrThrow());
    }

    public CompoundTag getPersistentData() {
        return persistentData;
    }

    public ResourceLocation getRegistryName() {
        return HDMRegistries.MOMENT_TYPE.getKey(type);
    }

    public Level getLevel() {
        return level;
    }

    public UUID getID() {
        return uuid;
    }

    public final void baseTick() {
        if (!isInitialized() || level.isClientSide) {
            return;
        }

        this.tick++;
        NeoForge.EVENT_BUS.post(new MomentEvent.Tick(this));

        if (state == MomentState.END) {
            return;
        }

        playerListManager.updatePlayers();

        updatePlayerIsInArea();
        updateMomentState();

    }

    private void updateMomentState() {
        MomentState previousState = state;

        // 初始状态设置（只在第一个tick执行）
        if (tick == 0L) {
            MomentEvent.Ready ready = (MomentEvent.Ready) setState(MomentState.READY);
            if (ready.isCanceled()) {
                setState(MomentState.END);
                return;
            }
        }

        switch (state) {
            case READY -> {
                MomentEvent.Start start = (MomentEvent.Start) setState(MomentState.START);
                if (start.isCanceled()) {
                    setState(MomentState.END);
                    break;
                }
                ready();
            }
            case START -> {
                setState(MomentState.ONGOING);
                start();
            }
            case ONGOING -> {
                ongoing();
            }
            case VICTORY -> {
                MomentEvent.Victory event = (MomentEvent.Victory) NeoForge.EVENT_BUS.post(MomentEvent.getEvent(this, MomentState.VICTORY));
                if (!event.isCanceled()) {
                    setState(MomentState.END);
                    victory();
                }
            }
            case LOSE -> {
                MomentEvent.Lose event = (MomentEvent.Lose) NeoForge.EVENT_BUS.post(MomentEvent.getEvent(this, MomentState.LOSE));
                if (!event.isCanceled()) {
                    setState(MomentState.END);
                    lose();
                }

            }
            case END -> {
                // 终止状态无需处理
                return;
            }
        }

        if (previousState != state) {
            LOGGER.debug("Moment state changed: {} -> {}", previousState, state);
        }
        tick();
    }

    protected void ready() {
    }

    protected void start() {

    }

    protected void ongoing() {

    }

    protected void victory() {
        moment.momentData().flatMap(MomentData::rewards)
                .ifPresent(rewards
                            -> playerListManager.players.forEach(player
                            -> rewards.forEach(reward
                            -> reward.createReward(this, player)
                        )
                    )
                );
    }

    protected void lose() {

    }

    public void tick() {

    }

    public void end() {
    }

    public MomentEvent setState(MomentState state) {
        this.state = state;
        playerListManager.getPlayers().forEach(player -> {
            if (!level.isClientSide) {
                PacketDistributor.sendToPlayer((ServerPlayer) player, new MomentStateSyncPayload(uuid, state));
            }
        });
        return NeoForge.EVENT_BUS.post(MomentEvent.getEvent(this, state));
    }

    public Predicate<Player> validPlayer() {
        return player -> !player.isSpectator();
    }





    private void updatePlayerIsInArea() {
        if (level.isClientSide) {
            return;
        }

        playerListManager.players.stream().filter(Objects::nonNull).forEach(player -> {
//            boolean inArea = moment.isInArea((ServerLevel) level, player.blockPosition());
//            boolean uuidContains = inAreaPlayers.contains(player.getUUID());
//            if (inArea && !uuidContains) {
//                onPlayerEnterArea((ServerPlayer) player);
//            } else if (!inArea && uuidContains) {
//                onPlayerExitArea((ServerPlayer) player);
//            }
        });
    }

    private void onPlayerExitArea(ServerPlayer player) {
        inAreaPlayers.remove(player.getUUID());
        NeoForge.EVENT_BUS.post(new PlayerMomentAreaEvent.Exit(player, this));
    }

    private void onPlayerEnterArea(ServerPlayer player) {
        inAreaPlayers.add(player.getUUID());
        NeoForge.EVENT_BUS.post(new PlayerMomentAreaEvent.Enter(player, this));
    }

    public MomentState getState() {
        return state;
    }

    public Set<Player> getPlayers() {
        return playerListManager.players;
    }

    public MomentBar getBar() {
        return bar;
    }

    public MomentType<?> getType() {
        return type;
    }

    public void finalizeSpawn(Entity entity) {
    }

    public void addKillCount(LivingEntity livingEntity, DamageSource source) {
        EntityTypeScoreTable entityTypeScoreTable = cacheProvider.getEntityTypeScoreTable();
        Integer score = entityTypeScoreTable.get(livingEntity.getType());

        KillEntityRecorderAttachment recorderAttachment = getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER).addKill(livingEntity, source, score);
        this.setData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER, recorderAttachment);

        if (level instanceof ServerLevel) {
            ServerPlayer serverPlayer = source.getEntity() instanceof ServerPlayer player ? player : null;
            BlockPos pos = serverPlayer == null ? null : serverPlayer.blockPosition();

            PacketDistributor.sendToAllPlayers(new KillEntityRecorderSyncPayload(KillEntityRecorderAttachment.KillType.MOMENT, uuid, recorderAttachment));
//            MomentInstanceManager momentInstanceManager1 = MomentInstanceManager.of(level);
//            momentInstanceManager1.trigger(KillEntityTrigger.class,
//                    new AutomationContext.Builder(level)
//                            .addPlayer(serverPlayer)
//                            .addEntityType(livingEntity.getType())
//                            .addMomentInstance(this)
//                            .build()
//            );

            if (serverPlayer != null) {
                playerListManager.addPlayerKillCount(serverPlayer, livingEntity, source, score);
            }
        }
    }

    public void livingDeath(LivingEntity entity, DamageSource source) {

    }

    public boolean canCreate(AutomationContext context) {
        return true;
    }

    public boolean checkGeneralConditions(AutomationContext context) {
        try {
            return moment.momentData()
                    .flatMap(MomentData::autoActuatorGroupSettings)
                    .flatMap(AutoActuatorGroupSettings::createRule)
                    .flatMap(AutomationRule::conditions)
                    .map(conditions -> conditions.stream().allMatch(condition -> condition.matches(context))).orElse(false);
        } catch (Exception e) {
            LOGGER.error("Exception occurred while checking conditions for MomentInstance", e);
            return false;
        }
    }

    public boolean isClientOnlyMoment() {
        return moment.isClientMomentInstanceOccupied();
    }


    public boolean isInitialized() {
        return initialized;
    }

    public MomentInstance initialize() {
        this.initialized = true;
        return this;
    }

    public boolean canSpawnEntity(Level level, Entity entity, BlockPos pos) {
        return true;
    }

    public void setSpawnPos(Entity entity) {
        ISpawnAlgorithm spawnAlgorithm = moment.momentData()
                .flatMap(MomentData::entitySpawnSettings)
                .flatMap(EntitySpawnSettings::spawnAlgorithm)
                .orElse(OpenAreaSpawnAlgorithm.DEFAULT);
        entity.setPos(spawnAlgorithm.spawn(this, entity));
    }

    public void spawnEntity(Entity entity) {
        setSpawnPos(entity);
        finalizeSpawn(entity);
        level.addFreshEntity(entity);
    }

    public void killAllEnemies(ServerLevel level) {
        enemiesManager.killAllEnemies(level);
    }

    public void addEnemy(Entity entity) {
        enemiesManager.addEnemy(entity);
        finalizeSpawn(entity);
    }

    public void removeEnemy(UUID uuid) {
        enemiesManager.removeEnemy(uuid);
    }

    public boolean hasEnemy(UUID uuid) {
        return enemiesManager.hasEnemy(uuid);
    }

    public int getEnemyCount() {
        return enemiesManager.size();
    }

    public Set<UUID> getEnemies() {
        return enemiesManager.getEnemies();
    }

    public ResourceLocation getMomentResource() {
        return HDMRegistries.MOMENT.getKey(moment);
    }

    public void refreshInstanceAfterPlayerUpdate(){
        updateTryRequiredKill();
        refreshBarAfterPlayerUpdate();
    }

    public void refreshBarAfterPlayerUpdate(){

    }

    public void updateTryRequiredKill() {
        HashMap<ResourceLocation, Pair<KillEntityCondition, KillEntityCondition.RequiredKill>> pairHashMap = new HashMap<>();
        tryRequiredKill.forEach((autoPair, killPair) -> {
            KillEntityCondition.RequiredKill killRecord = killPair.getFirst().getKillRecord(this);
            pairHashMap.put(autoPair, new Pair<>(killPair.getFirst(), killRecord));

            if (!level.isClientSide) {
                getPlayers().forEach(player -> {
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new KillRequiredSyncPayload(uuid, autoPair, killPair.getFirst(), killRecord));
                });
            }
        });

        this.tryRequiredKill = pairHashMap;


    }

    public Pair<KillEntityCondition, KillEntityCondition.RequiredKill> getTryRequiredKill(ResourceLocation location) {
        return tryRequiredKill.get(location);
    }

    public void setTryRequiredKill(ResourceLocation location,KillEntityCondition killEntityCondition, KillEntityCondition.RequiredKill requiredKill) {
        this.tryRequiredKill.put(location, new Pair<>(killEntityCondition,requiredKill));

        if (!level.isClientSide){
            getPlayers().forEach(player -> {
                PacketDistributor.sendToPlayer((ServerPlayer) player, new KillRequiredSyncPayload(uuid,location,killEntityCondition,requiredKill));
            });
        }
    }
}
