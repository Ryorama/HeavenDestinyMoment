package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMActuators;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;

import java.util.Objects;

public final class StateSettingActuator implements IActuator {
    public static final MapCodec<StateSettingActuator> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            MomentState.CODEC.fieldOf("state").forGetter(StateSettingActuator::state)
    ).apply(instance, StateSettingActuator::new));
    private final MomentState state;

    public StateSettingActuator(MomentState state) {
        this.state = state;
    }

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

    public MomentState state() {
        return state;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StateSettingActuator) obj;
        return Objects.equals(this.state, that.state);
    }

}
