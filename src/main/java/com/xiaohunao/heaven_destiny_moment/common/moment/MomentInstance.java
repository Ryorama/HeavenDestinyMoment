package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntityTypeScoreTable;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.event.MomentEvent;
import com.xiaohunao.heaven_destiny_moment.common.event.PlayerMomentAreaEvent;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMTriggerTypes;
import com.xiaohunao.heaven_destiny_moment.common.network.KillEntityRecorderSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentStateSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentUpdatePlayersPayload;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.OpenAreaSpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.ConditionalTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.KillEntityTrigger;
import com.xiaohunao.heaven_destiny_moment.common.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class MomentInstance extends AttachmentHolder {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final MomentInstanceManager momentInstanceManager;
    protected final Level level;
    protected final MomentType<?> type;
    protected final UUID uuid;
    protected final Moment moment;


    private boolean initialized = false;


    protected MomentBar bar;
    protected long tick = -1L;
    protected MomentState state = MomentState.UNINITIALIZED;
    protected Set<UUID> playerUUIDs = Sets.newHashSet();
    protected Set<Player> players = Sets.newHashSet();
    protected Set<UUID> inAreaPlayers = Sets.newHashSet();
    protected Set<Vec3> spawnPosList = Sets.newHashSet();
    protected CompoundTag persistentData = new CompoundTag();
    protected final EnemiesManager enemiesManager = new EnemiesManager();
    protected Map<IActuator,KillEntityCondition.RequiredKill> tryRequiredKill = new ConcurrentHashMap<>();

    protected MomentInstance(MomentType<?> type, Level level, Moment moment) {
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.level = level;
        this.moment = moment;
        this.momentInstanceManager = null;
    }

    protected MomentInstance(MomentType<?> type, UUID uuid, Level level, Moment moment) {
        this.uuid = uuid;
        this.type = type;
        this.level = level;
        this.moment = moment;
        this.momentInstanceManager = null;
    }

    protected MomentInstanceManager getMomentManager() {
        if (this.momentInstanceManager == null) {
            return MomentInstanceManager.of(level);
        }
        return this.momentInstanceManager;
    }

    public Moment getMoment() {
        return moment;
    }

    public void init() {
        initMomentBar();
        initSpawnPosList();
        initTryModifyStateRequiredKill();
    }

    public void initMomentBar() {
        moment.barRenderType.ifPresent(iBarRenderType -> this.bar = new MomentBar(uuid, iBarRenderType));
    }

    public void initSpawnPosList() {

    }

    public void initTryModifyStateRequiredKill(){
        moment.momentData().flatMap(MomentData::autoActuatorGroupSettings).ifPresent(autoActuatorGroupSettings -> {
            autoActuatorGroupSettings.autoActuators().forEach((triggerContext, actuatorContext) -> {
                if (triggerContext.trigger() instanceof ConditionalTrigger conditionalTrigger) {
                    conditionalTrigger.conditions().forEach(condition -> {
                        if (condition instanceof KillEntityCondition killEntityCondition) {
                            KillEntityCondition.RequiredKill killRecord = killEntityCondition.getKillRecord(level);
                            tryRequiredKill.put(actuatorContext.actuator(), killRecord);
                        }
                    });
                } else {
                    triggerContext.conditions().forEach(condition -> {
                        if (condition instanceof KillEntityCondition killEntityCondition) {
                            KillEntityCondition.RequiredKill killRecord = killEntityCondition.getKillRecord(level);
                            tryRequiredKill.put(actuatorContext.actuator(), killRecord);
                        }
                    });
                }
            });
        });
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
            this.bar.updateProgress(progress);
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
                    return momentType.create(compoundTag.getUUID("uuid"), level,
                            HDMRegistries.MOMENT.byNameCodec().decode(NbtOps.INSTANCE,tag).getOrThrow().getFirst()
                    );
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
        if (attachments != null) compoundTag.put(ATTACHMENTS_NBT_KEY, attachments);

        compoundTag.put("enemies_manager", enemiesManager.serializeNBT());
        compoundTag.put("persistentData", this.persistentData);
        compoundTag.putLong("tick", tick);
        if (this.bar != null) {
            compoundTag.put("bar", MomentBar.CODEC.encodeStart(NbtOps.INSTANCE, this.bar).getOrThrow());
        }

        if (state != null) {
            compoundTag.putString("state", state.name());
        }

        ListTag playerUUIDTags = new ListTag();
        playerUUIDs.forEach(uuid -> playerUUIDTags.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("player_uuids", playerUUIDTags);

        ListTag spawnPosListTag = new ListTag();
        spawnPosList.forEach(vec3 -> spawnPosListTag.add(Vec3.CODEC.encodeStart(NbtOps.INSTANCE, vec3).getOrThrow()));
        compoundTag.put("spawnPosList", spawnPosListTag);

        if (tryRequiredKill != null) {
            CodecUtils.complexKeyMap(IActuator.CODEC,KillEntityCondition.RequiredKill.CODEC)
                    .encodeStart(NbtOps.INSTANCE, tryRequiredKill).result()
                    .ifPresent(tryRequiredKill -> compoundTag.put("tryRequiredKill", tryRequiredKill));
        }


        return compoundTag;
    }


    public void deserializeNBT(CompoundTag compoundTag) {
        if (compoundTag.contains(ATTACHMENTS_NBT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) deserializeAttachments(level.registryAccess(), compoundTag.getCompound(ATTACHMENTS_NBT_KEY));
        enemiesManager.deserializeNBT(compoundTag.getCompound("enemies_manager"));
        this.persistentData = compoundTag.getCompound("persistentData");
        this.tick = compoundTag.getLong("tick");

        if (compoundTag.contains("bar")) {
            this.bar = MomentBar.CODEC.decode(NbtOps.INSTANCE, compoundTag.get("bar")).getOrThrow().getFirst();
        }

        if (compoundTag.contains("state")) {
            this.state = MomentState.valueOf(compoundTag.getString("state"));
        }

        ListTag playerUUIDTags = compoundTag.getList("player_uuids", Tag.TAG_LIST);
        playerUUIDTags.forEach(tag -> playerUUIDs.add(UUID.fromString(tag.getAsString())));

        ListTag spawnPosListTag = compoundTag.getList("spawnPosList", Tag.TAG_LIST);
        spawnPosListTag.forEach(tag -> spawnPosList.add(Vec3.CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst()));

        if (compoundTag.contains("tryRequiredKill")) {
            this.tryRequiredKill.clear();
            Map<IActuator, KillEntityCondition.RequiredKill> decodedMap = CodecUtils.complexKeyMap(IActuator.CODEC, KillEntityCondition.RequiredKill.CODEC)
                    .decode(NbtOps.INSTANCE, compoundTag.get("tryRequiredKill"))
                    .getOrThrow().getFirst();
            this.tryRequiredKill.putAll(decodedMap);
        }
    }


    private void serializeMetaData(CompoundTag compoundTag) {
        compoundTag.putUUID("uuid", uuid);
        compoundTag.putString("id", MomentInstance.getRegistryName(type).toString());
        compoundTag.put("moment", ResourceKey.codec(HDMRegistries.Keys.MOMENT).encodeStart(NbtOps.INSTANCE, HDMRegistries.MOMENT.getResourceKey(moment).get()).getOrThrow());
    }

    public CompoundTag getPersistentData() {
        return persistentData;
    }

    public static ResourceLocation getRegistryName(MomentType<?> momentType) {
        return HDMRegistries.MOMENT_TYPE.getKey(momentType);
    }

    public Level getLevel() {
        return level;
    }

    public UUID getID() {
        return uuid;
    }

    public final void baseTick() {
        if(!isInitialized()) return;

        this.tick++;
        NeoForge.EVENT_BUS.post(new MomentEvent.Tick(this));

        if (state == MomentState.END) return;

        if (!level.isClientSide){
            updatePlayers();

        }

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
                MomentEvent.Victory event = (MomentEvent.Victory) NeoForge.EVENT_BUS.post(MomentEvent.getEventToPost(this, MomentState.VICTORY));
                if (!event.isCanceled()) {
                    setState(MomentState.END);
                    victory();
                }
            }
            case LOSE -> {
                MomentEvent.Lose event = (MomentEvent.Lose) NeoForge.EVENT_BUS.post(MomentEvent.getEventToPost(this, MomentState.LOSE));
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
        moment.momentData.flatMap(MomentData::rewards)
                .ifPresent(rewards ->
                    players.forEach(player ->
                            rewards.forEach(reward ->
                                    reward.createReward(this, player)
                            )
                    )
                );
    }

    protected void lose() {

    }

    public void tick() {

    }

    public void end() {}


    public MomentEvent setState(MomentState state) {
        this.state = state;
        moment.tipSettings.ifPresent(tip -> tip.playTooltip(this));
        if (!level.isClientSide){
            players.forEach(player ->{
                PacketDistributor.sendToPlayer((ServerPlayer) player,new MomentStateSyncPayload(uuid,state));
            });
        }
        return NeoForge.EVENT_BUS.post(MomentEvent.getEventToPost(this, state));
    }


    public Predicate<Player> validPlayer() {
        return player -> !player.isSpectator();
    }

    public List<Player> getPlayers(Predicate<? super Player> predicate) {
        List<Player> list = Lists.newArrayList();

        for (Player player : level.players()) {
            if (predicate.test(player)) {
                list.add(player);
            }
        }
        return list;
    }

    public void updatePlayers() {


        final Set<Player> oldPlayers = Sets.newHashSet(players);
        final Set<Player> newPlayers = Sets.newHashSet((getPlayers(validPlayer())));

        newPlayers.stream()
                .filter(player -> !oldPlayers.contains(player))
                .forEach(player1 -> {
                    getMomentManager().addPlayerToInstance(player1, this);
                    players.add(player1);
                    playerUUIDs.add(player1.getUUID());
                });
        oldPlayers.stream()
                .filter(player -> !newPlayers.contains(player))
                .forEach(player1 -> {
                    getMomentManager().removePlayerToInstance(player1, this);
                    players.remove(player1);
                    playerUUIDs.add(player1.getUUID());
                });

        if (!level.isClientSide){
            PacketDistributor.sendToAllPlayers(new MomentUpdatePlayersPayload(uuid));
        }


    }

    private void updatePlayerIsInArea() {
        if (level.isClientSide) return;

        players.stream().filter(Objects::nonNull).forEach(player -> {
            boolean inArea = moment.isInArea((ServerLevel) level, player.blockPosition());
            boolean uuidContains = inAreaPlayers.contains(player.getUUID());
            if (inArea && !uuidContains) {
                onPlayerEnterArea((ServerPlayer) player);
            } else if (!inArea && uuidContains) {
                onPlayerExitArea((ServerPlayer) player);
            }
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
        return players;
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
        EntityTypeScoreTable entityTypeScoreTable = moment.momentData.flatMap(MomentData::entityTypeScoreTable).orElse(new EntityTypeScoreTable.Builder().build());
        Integer score = entityTypeScoreTable.get(livingEntity.getType());
        KillEntityRecorderAttachment recorderAttachment = getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER).addKill(livingEntity, source, score);
        this.setData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER, recorderAttachment);
        if (level instanceof ServerLevel serverLevel){
            ServerPlayer serverPlayer = source.getEntity() instanceof ServerPlayer player ? player : null;
            BlockPos pos = serverPlayer == null ? null : serverPlayer.blockPosition();

            PacketDistributor.sendToPlayersInDimension(serverLevel,new KillEntityRecorderSyncPayload(KillEntityRecorderAttachment.KillType.MOMENT,uuid,recorderAttachment));
            TriggerTypeManager.trigger(HDMTriggerTypes.KILL_ANY_ENTITY_MOMENT.get(), level, KillEntityTrigger::canTrigger, pos, serverPlayer);
        }
    }

    public void livingDeath(LivingEntity entity,DamageSource source) {

    }

    public boolean canCreate(Map<UUID, MomentInstance> runMoments, Level level, @Nullable BlockPos pos, @Nullable ServerPlayer player) {
        return true;
    }

    public boolean checkGeneralConditions(@Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
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
                                        if (condition != null && !condition.matches(this, pos, serverPlayer)) {
                                            return false;
                                        }
                                    }
                                }
                            }

                            if (actuatorContext.actuator() instanceof CreateMomentInstanceActuator) {
                                List<ICondition> conditions = triggerContext.conditions();
                                if (conditions != null) {
                                    for (ICondition condition : conditions) {
                                        if (condition != null && !condition.matches(this, pos, serverPlayer)) {
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

    public Player getRandomPlayer() {
        if (players.isEmpty()) {
            return null;
        }

        List<Player> playerList = Lists.newArrayList(players);
        return playerList.get(level.random.nextInt(playerList.size()));
    }

    public boolean isClientOnlyMoment() {
        return moment.isClientMomentInstanceOccupied();
    }

    public void registerTracker(){
        moment.trackers.ifPresent(trackers -> trackers.forEach(ITracker::register));
    }

    public void unregisterTracker(){
        moment.trackers.ifPresent(trackers -> trackers.forEach(ITracker::unregister));
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

    public void setEntityTagMark(Entity entity) {
        entity.setData(HDMAttachments.MOMENT_ENTITY, entity.getData(HDMAttachments.MOMENT_ENTITY).setUid(this.uuid));
    }

    public void setSpawnPos(Entity entity) {
        ISpawnAlgorithm spawnAlgorithm = moment.momentData
                .flatMap(MomentData::entitySpawnSettings)
                .flatMap(EntitySpawnSettings::spawnAlgorithm)
                .orElse(OpenAreaSpawnAlgorithm.DEFAULT);
        entity.setPos(spawnAlgorithm.spawn(this, entity));
    }

    public void spawnEntity(Entity entity) {
        setEntityTagMark(entity);
        setSpawnPos(entity);
        finalizeSpawn(entity);
        level.addFreshEntity(entity);
    }

    public void killAllEnemies(ServerLevel level){
        enemiesManager.killAllEnemies(level);
    }

    public void clearAllEnemiesFlags(ServerLevel level){
        enemiesManager.clearAllEnemiesFlags(level);
    }

    public void addEnemy(Entity entity) {
        enemiesManager.addEnemy(entity);
        setEntityTagMark(entity);
        finalizeSpawn(entity);
    }

    public void removeEnemy(UUID uuid) {
        enemiesManager.removeEnemy(uuid);
    }

    public void markEnemyAsLoaded(UUID uuid) {
        enemiesManager.markEntityAsLoaded(uuid);
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

    public void setVictoryRequiredKill(IActuator actuator, KillEntityCondition.RequiredKill requiredKill) {
        this.tryRequiredKill.put(actuator, requiredKill);
    }

    public Map<IActuator, KillEntityCondition.RequiredKill> getTryRequiredKill() {
        return tryRequiredKill;
    }
}
