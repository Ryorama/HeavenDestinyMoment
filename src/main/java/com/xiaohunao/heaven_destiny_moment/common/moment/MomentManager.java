package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.mixed.ClientMomentInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.network.ClientOnlyMomentSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentBarSyncPayload;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MomentManager {
    private final Level level;

    private final ConcurrentHashMap<UUID, MomentInstance<?>> runMoments = new ConcurrentHashMap<>();
    private final HashMultimap<ResourceKey<Moment<?>>,MomentInstance<?>> momentMap = HashMultimap.create();
    private final Multimap<UUID, MomentInstance<?>> playerMoments = HashMultimap.create();


    public MomentManager(Level level) {
        this.level = level;
    }

    public static MomentManager of(Level level) {
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
                    runMoments.put(momentInstance.getID(), momentInstance);
                    momentMap.put(momentInstance.getResourceKey(), momentInstance);
                });

            });
        }
    }

    public MomentInstance<?> getMomentInstance(UUID uuid) {
        return runMoments.get(uuid);
    }

    public Set<MomentInstance<?>> getMomentInstances(ResourceKey<Moment<?>> key) {
        return momentMap.get(key);
    }

    public Collection<MomentInstance<?>> getMomentInstances() {
        return ImmutableList.copyOf(runMoments.values());
    }

    public void tick() {
//        if (level.getServer() != null && level.getServer().getPlayerCount() == 0){
//            return;
//        }

        runMoments.values().forEach(instance -> {
            if (instance.state == MomentState.END) {
                instance.end();
                removeMomentInstance(instance, true);
            }
            instance.baseTick();
        });
    }

    public void addMomentInstance(MomentInstance<?> instance, boolean isSync) {
        runMoments.put(instance.getID(), instance);
        momentMap.put(instance.getResourceKey(), instance);

        if (isSync && !level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),false));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.addPlayer(instance.getBar()));
            }
        }
    }

    public void removeMomentInstance(MomentInstance<?> instance, boolean isSync) {
        runMoments.remove(instance.getID());
        momentMap.remove(instance.getResourceKey(), instance);

        if (isSync && !level.isClientSide) {
            PacketDistributor.sendToAllPlayers(new MomentManagerSyncPayload(instance.serializeNBT(),true));
            PacketDistributor.sendToAllPlayers(new ClientOnlyMomentSyncPayload(instance.serializeNBT(), true));
            if (instance.getBar() != null) {
                PacketDistributor.sendToAllPlayers(MomentBarSyncPayload.removePlayer(instance.bar));
            }
        }
    }

    public Optional<MomentInstance<?>> createMomentInstance(ResourceKey<Moment<?>> momentKey, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return createMomentInstance(momentKey, pos, serverPlayer, null);
    }

    public Optional<MomentInstance<?>> createMomentInstance(ResourceKey<Moment<?>> momentKey, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer,@Nullable Consumer<MomentInstance<?>> modifier) {
        return Optional.of(level.registryAccess().registryOrThrow(HDMRegistries.Keys.MOMENT))
                .map(registry -> registry.get(momentKey))
                .map(moment -> moment.newMomentInstance(level, momentKey))
                .map(instance -> {
                    Optional.ofNullable(modifier).ifPresent(consumer -> consumer.accept(instance));
                    return instance;
                })
                .map(instance -> {
                    instance.updatePlayers();

                    boolean conditionMatch = instance.moment()
                            .flatMap(Moment::momentData)
                            .flatMap(MomentData::conditionGroup)
                            .map( conditionGroup-> conditionGroup.matchCreate(instance, pos, serverPlayer))
                            .orElse(true);

                    boolean canCreate = instance.canCreate(runMoments, level, pos, serverPlayer);

                    if (canCreate && conditionMatch) {
                        instance.init();
                        addMomentInstance(instance, true);
                        return instance;
                    }
                    return null;
                });
    }


    public boolean hasMoment(ResourceKey<Moment<?>> key) {
        return momentMap.containsKey(key);
    }

    public boolean addPlayerToInstance(Player player, MomentInstance<?> instance) {
        if (player == null || instance == null) {
            return false;
        }

        UUID uuid = player.getUUID();

        if (!playerMoments.containsKey(uuid)) {
            addPlayerAndSync(player, instance);
            return true;
        }

        boolean canAddPlayer = true;
        for (MomentInstance<?> existingInstance : playerMoments.get(uuid)) {
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

    private void addPlayerAndSync(Player player, MomentInstance<?> instance){
        playerMoments.put(player.getUUID(), instance);

        if (!level.isClientSide && instance.isClientOnlyMoment()) {
            PacketDistributor.sendToPlayer((ServerPlayer) player, new ClientOnlyMomentSyncPayload(instance.serializeNBT(), false));
        }
    }

    public boolean removePlayerToMoment(Player player, MomentInstance<?> instance) {
        if (player == null || instance == null) {
            return false;
        }
        UUID uuid = player.getUUID();
        if (!playerMoments.containsKey(uuid)) {
            return false;
        }

        return playerMoments.remove(uuid, instance);
    }

    public Optional<MomentInstance<?>> getClientMomentInstance() {
        if (!level.isClientSide){
            return Optional.empty();

        }

        return Optional.ofNullable(((ClientMomentInstanceMixed) level).heaven_destiny_moment$getClientMomentInstance());
    }

    public void setClientMomentInstance(MomentInstance<?> momentInstance) {
        if (!level.isClientSide){
            return;
        }

        ((ClientMomentInstanceMixed) level).heaven_destiny_moment$setClientMomentInstance(momentInstance);
    }
}
