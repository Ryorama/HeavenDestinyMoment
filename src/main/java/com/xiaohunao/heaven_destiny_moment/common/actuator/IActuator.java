package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;

import java.util.function.Function;

public interface IActuator {
    Codec<IActuator> CODEC = Codec.lazyInitialized(HDMRegistries.ACTUATOR_CODEC::byNameCodec).dispatch(IActuator::codec, Function.identity());

    MapCodec<? extends IActuator> codec();

    void execute(MomentInstance momentInstance);
}
