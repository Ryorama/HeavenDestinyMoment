package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMActuators;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;

public record CreateMomentInstanceActuator() implements IActuator {
    public static final MapCodec<CreateMomentInstanceActuator> CODEC = MapCodec.unit(CreateMomentInstanceActuator::new);

    @Override
    public MapCodec<? extends IActuator> codec() {
        return HDMActuators.CREATE_MOMENT_INSTANCE_ACTUATOR.get();
    }

    @Override
    public void execute(MomentInstance momentInstance) {

    }
}
