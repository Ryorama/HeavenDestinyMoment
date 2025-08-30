package com.xiaohunao.heaven_destiny_moment.common.context.attachable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public interface IAttachable extends ICodec<IAttachable> {
    Codec<IAttachable> CODEC = Codec.lazyInitialized(HDMRegistries.ATTACHABLE_CODEC::byNameCodec).dispatch(IAttachable::codec, Function.identity());

    void attachToEntity(LivingEntity livingEntity);

    MapCodec<? extends IAttachable> codec();
}
