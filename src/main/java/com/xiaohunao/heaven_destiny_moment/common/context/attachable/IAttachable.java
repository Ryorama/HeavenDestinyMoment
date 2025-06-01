package com.xiaohunao.heaven_destiny_moment.common.context.attachable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public interface IAttachable {
    Codec<IAttachable> CODEC = Codec.lazyInitialized(HDMRegistries.ATTACHABLE_CODEC::byNameCodec).dispatch(IAttachable::codec, Function.identity());

    void attachToEntity(LivingEntity livingEntity);

    MapCodec<? extends IAttachable> codec();
}
