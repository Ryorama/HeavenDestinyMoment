package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.*;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.ConditionGroup;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.AutoProbabilityCondition;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerContainer;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MomentManager extends SavedData {
    private static final String NAME = HeavenDestinyMoment.MODID + "_moment_manager";

    private final Map<UUID, MomentInstance<?>> runMoments = Maps.newHashMap();
    private final Set<ResourceKey<Moment<?>>> runMomentKeyes = Sets.newHashSet();
    private MomentInstance<?> clientOnlyMoment = null;
    private final Multimap<UUID, MomentInstance<?>> playerMoments = HashMultimap.create();

    private MomentManager clientMonger;
    private Level level;


    public static MomentManager of(Level level) {
        MomentManagerContainer container = (MomentManagerContainer) level;
        MomentManager momentManager = container.heaven_destiny_moment$getMomentManager();
        if (level instanceof ServerLevel serverLevel) {
            if (momentManager == null) {
                momentManager = serverLevel.getDataStorage().computeIfAbsent(new Factory<>(MomentManager::new,
                        (CompoundTag compoundTag, HolderLookup.Provider tag) -> load(serverLevel, compoundTag, tag)), NAME);
                container.heaven_destiny_moment$setMomentManager(momentManager);
                momentManager.level = serverLevel;
            }
        } else {
            if (momentManager == null) {
                momentManager = new MomentManager();
                container.heaven_destiny_moment$setMomentManager(momentManager);
            }
            momentManager.level = level;
        }
        return momentManager;
    }


    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        runMoments.values().forEach(momentInstance -> listTag.add(momentInstance.serializeNBT()));
        compoundTag.put("moment", listTag);
        return compoundTag;
    }

    public static MomentManager load(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider lookupProvider) {
        MomentManager manager = new MomentManager();
        ListTag listTag = tag.getList("moment", Tag.TAG_COMPOUND);
        listTag.forEach(compoundTag -> {
            MomentInstance<?> instance = MomentInstance.loadStatic(serverLevel, (CompoundTag) compoundTag);
            if (instance != null){
                manager.addMomentInstance(instance, true);
            }
        });
        return manager;
    }

    public void tick() {
        CopyOnWriteArrayList<MomentInstance<?>> momentInstances = new CopyOnWriteArrayList<>(runMoments.values());
        momentInstances.forEach(instance -> {
            if (instance.state == MomentState.END) {
                instance.end();
                removeMomentInstance(instance, true);
            }
            instance.baseTick();
        });
        setDirty();
    }

    public void removeMomentInstance(MomentInstance<?> instance, boolean isSync) {
        Level level = instance.getLevel();
        runMoments.remove(instance.getID());
        runMomentKeyes.remove(instance.momentKey);
        instance.players.forEach(player -> {
            if (playerMoments.containsKey(player.getUUID())) {
                playerMoments.remove(player.getUUID(), instance);
            }
        });

        if (isSync && !level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),true));
            PacketDistributor.sendToAllPlayers(new ClientOnlyMomentSyncPayload(instance.serializeNBT(), true));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.removePlayer(instance.bar));
            }
            serverLevel.getDataStorage().save();
        }
    }

    public void addMomentInstance(MomentInstance<?> instance,boolean isSync) {
        Level level = instance.getLevel();
        UUID uuid = instance.getID();
        runMoments.put(uuid, instance);
        runMomentKeyes.add(instance.momentKey);
        setDirty();

        if (isSync && !level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),false));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.addPlayer(instance.getBar()));
            }
            serverLevel.getDataStorage().save();
        }
    };


    public MomentInstance<?> addMoment(MomentInstance<?> instance, ServerLevel serverLevel, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        UUID uuid = instance.getID();

        instance.updatePlayers();

        Boolean conditionMatch = instance.moment()
                .flatMap(Moment::momentData)
                .flatMap(MomentData::conditionGroup)
                .flatMap(ConditionGroup::create)
                .map(createGroup -> {
                    return createGroup.getSecond().stream()
                            .filter(condition -> !(condition instanceof AutoProbabilityCondition))
                            .allMatch(condition -> condition.matches(instance, pos, serverPlayer));
                })
                .orElse(true);
        boolean canCreate = instance.canCreate(runMoments, serverLevel, pos, serverPlayer);
        if (canCreate && conditionMatch) {
            instance.init();
            addMomentInstance(instance,true);
            return instance;
        }
        return null;
    }

    public boolean addPlayerToMoment(Player player, MomentInstance<?> instance) {
        UUID playerUUID = player.getUUID();

        if (!playerMoments.containsKey(playerUUID)) {
            playerMoments.put(playerUUID, instance);
            if (!instance.getLevel().isClientSide && instance.isClientOnlyMoment()) {
                PacketDistributor.sendToPlayersInDimension((ServerLevel) instance.getLevel(), new ClientOnlyMomentSyncPayload(instance.serializeNBT(), false));
            }
            setDirty();
            return true;
        }

        boolean canAddPlayer = true;
        for (MomentInstance<?> momentInstance : playerMoments.get(playerUUID)) {
            Optional<? extends Moment<?>> moment = momentInstance.moment();
            if (moment.isPresent()) {
                if (momentInstance.getID().equals(instance.getID())) break;

                Boolean isEmpty = moment.flatMap(Moment::clientSettings).map(ClientSettings::isPresent).orElse(true);

                if (!isEmpty) {
                    canAddPlayer = false;
                    break;
                }
            }
        }

        if (canAddPlayer) {
            playerMoments.put(playerUUID, instance);
            Boolean isEmpty = instance.moment().flatMap(Moment::clientSettings).map(ClientSettings::isPresent).orElse(true);
            if (isEmpty) {
                if (!instance.getLevel().isClientSide && instance.isClientOnlyMoment()) {
                    PacketDistributor.sendToPlayersInDimension((ServerLevel) instance.getLevel(), new ClientOnlyMomentSyncPayload(instance.serializeNBT(), false));
                }
            }
            setDirty();
            return true;
        }

        return false;
    }

    public boolean removePlayerToMoment(Player player, MomentInstance<?> instance) {
        UUID playerUUID = player.getUUID();
        if (playerMoments.containsKey(playerUUID)) {
            playerMoments.remove(playerUUID, instance);
            setDirty();
            return true;
        }
        return false;
    }

    public void setClientOnlyMoment(MomentInstance<?> clientOnlyMoment) {
        this.clientOnlyMoment = clientOnlyMoment;
        setDirty();
    }

    public Optional<MomentInstance<?>> getClientOnlyMoment() {
        return Optional.ofNullable(clientOnlyMoment);
    }

    public Collection<MomentInstance<?>> getPlayerMoment(UUID playerUUID) {
        return playerMoments.get(playerUUID);
    }

    public Map<UUID, MomentInstance<?>> getImmutableRunMoments() {
        return ImmutableMap.copyOf(runMoments);
    }

    public boolean hasMoment(ResourceKey<Moment<?>> key) {
        return runMomentKeyes.contains(key);
    }

    public MomentInstance<?> getMomentInstance(UUID uuid){
        return runMoments.get(uuid);
    }

    public void sync(MomentInstance<?> instance, ServerLevel serverLevel) {
        if (isDirty()) {
            serverLevel.getDataStorage().save();
        }
        PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),false));
        if (instance.getBar() != null) {
            PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.addPlayer(instance.getBar()));
        }
    }
}
