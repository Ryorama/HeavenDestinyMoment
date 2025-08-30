package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMActuators;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;

public record CreateMomentInstanceActuator() implements IActuator {
    public static final MapCodec<CreateMomentInstanceActuator> CODEC = MapCodec.unit(CreateMomentInstanceActuator::new);

    @Override
    public MapCodec<? extends IActuator> codec() {
        return CODEC;
    }

    @Override
    public void execute(MomentInstance momentInstance) {

    }
}
