package com.xiaohunao.heaven_destiny_moment.compat.kubejs.builder;


import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import com.xiaohunao.heaven_destiny_moment.compat.kubejs.KubeJSMoment;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MomentTypeKubeJSBuilder extends BuilderBase<MomentType<?>> {
    private final Map<String, KubeJSMoment.MomentInstanceCallback<?, ?>> callbacks = new HashMap<>();

    public MomentTypeKubeJSBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public MomentType<?> createObject() {
        return new MomentType.Builder<>((uuid, level, key) -> {
            KubeJSMoment.KubeJSMomentInstance instance = new KubeJSMoment.KubeJSMomentInstance(uuid, level, key);
            callbacks.forEach(instance::registerCallback);
            return instance;
        }).build();
    }

    public MomentTypeKubeJSBuilder onInit(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("init", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onStart(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("start", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onOngoing(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("ongoing", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onVictory(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("victory", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onLose(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("lose", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onTick(Consumer<KubeJSMoment.KubeJSMomentInstance> handler) {
        callbacks.put("tick", (instance, param) -> {
            handler.accept(instance);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onFinalizeSpawn(BiConsumer<KubeJSMoment.KubeJSMomentInstance, Entity> handler) {
        callbacks.put("finalizeSpawn", (instance, param) -> {
            handler.accept(instance, (Entity) param);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder onLivingDeath(BiConsumer<KubeJSMoment.KubeJSMomentInstance, LivingEntity> handler) {
        callbacks.put("livingDeath", (instance, param) -> {
            handler.accept(instance, (LivingEntity) param);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder setRandomSpawnPos(Function<KubeJSMoment.KubeJSMomentInstance, Vec3> handler) {
        callbacks.put("getRandomSpawnPos", (instance, param) -> handler.apply(instance));
        return this;
    }

    public MomentTypeKubeJSBuilder setValidPlayer(Function<KubeJSMoment.KubeJSMomentInstance, Predicate<Player>> handler) {
        callbacks.put("validPlayer", (instance, param) -> handler.apply(instance));
        return this;
    }

    public MomentTypeKubeJSBuilder onSerializeNBT(Function<KubeJSMoment.KubeJSMomentInstance, CompoundTag> handler) {
        callbacks.put("serializeNBT", (instance, param) -> handler.apply(instance));
        return this;
    }

    public MomentTypeKubeJSBuilder onDeserializeNBT(BiConsumer<KubeJSMoment.KubeJSMomentInstance, CompoundTag> handler) {
        callbacks.put("deserializeNBT", (instance, param) -> {
            handler.accept(instance, (CompoundTag) param);
            return null;
        });
        return this;
    }

    public MomentTypeKubeJSBuilder setCanCreate(CanCreateCallback handler) {
        callbacks.put("canCreate", (instance, param) -> {
            KubeJSMoment.CreateParams params = (KubeJSMoment.CreateParams) param;
            return handler.canCreate(instance, params.runMoments(), params.level(), params.pos(), params.player());
        });
        return this;
    }

    public MomentTypeKubeJSBuilder setCanSpawnEntity(CanSpawnEntityCallback handler) {
        callbacks.put("canSpawnEntity", (instance, param) -> {
            KubeJSMoment.SpawnParams params = (KubeJSMoment.SpawnParams) param;
            return handler.canSpawnEntity(instance, params.level(), params.entity(), params.pos());
        });
        return this;
    }

    @FunctionalInterface
    public interface CanCreateCallback extends CallbackSerializable{
        boolean canCreate(KubeJSMoment.KubeJSMomentInstance instance, Map<UUID, MomentInstance<?>> runMoments,
                          Level level, @Nullable BlockPos pos, @Nullable ServerPlayer player);
    }

    @FunctionalInterface
    public interface CanSpawnEntityCallback extends CallbackSerializable {
        boolean canSpawnEntity(KubeJSMoment.KubeJSMomentInstance instance, Level level, Entity entity, BlockPos pos);
    }
}
