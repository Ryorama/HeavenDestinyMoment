package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;

import java.util.Optional;
import java.util.function.Function;

public interface IMoment {
    Codec<Moment> CODEC = Codec.lazyInitialized(HDMRegistries.MOMENT_CODEC::byNameCodec).dispatch(IMoment::codec, Function.identity());


    MapCodec<? extends Moment> codec();

    Optional<IBarRenderType> barRenderType();
//
//    CompoundTag serializeNBT();
//
//    void deserializeNBT(CompoundTag compoundTag);
}
