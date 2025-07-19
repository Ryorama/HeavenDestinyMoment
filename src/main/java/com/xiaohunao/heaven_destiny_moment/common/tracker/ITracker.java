package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;
import java.util.function.Function;

public interface ITracker {
    Codec<ITracker> CODEC = Codec.lazyInitialized(HDMRegistries.TRACKER_CODEC::byNameCodec).dispatch(ITracker::codec, Function.identity());

    void register(UUID instanceUUID);

    void unregister();

    MapCodec<? extends ITracker> codec();
}
