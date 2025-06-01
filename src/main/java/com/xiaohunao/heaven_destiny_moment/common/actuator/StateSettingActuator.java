package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMActuators;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;

public record StateSettingActuator(MomentState state) implements IActuator {
    public static final MapCodec<StateSettingActuator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MomentState.CODEC.fieldOf("state").forGetter(StateSettingActuator::state)
    ).apply(instance, StateSettingActuator::new));

    public static StateSettingActuator of(MomentState state) {
        return new StateSettingActuator(state);
    }


    @Override
    public MapCodec<? extends IActuator> codec() {
        return HDMActuators.STATE_SETTING_ACTUATOR.get();
    }

    @Override
    public void execute(MomentInstance momentInstance) {
        momentInstance.setState(state);
    }
}
