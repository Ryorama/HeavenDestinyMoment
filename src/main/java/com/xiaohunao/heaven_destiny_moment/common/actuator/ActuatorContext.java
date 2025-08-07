package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public final class ActuatorContext {
    public static final Codec<ActuatorContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IActuator.CODEC.fieldOf("actuator").forGetter(ActuatorContext::actuator),
            Codec.INT.fieldOf("max_executions").forGetter(ActuatorContext::max_executions)
    ).apply(instance, ActuatorContext::new));
    private final IActuator actuator;
    private final Integer max_executions;

    public ActuatorContext(IActuator actuator, Integer max_executions) {
        this.actuator = actuator;
        this.max_executions = max_executions;
    }

    public static ActuatorContext of(IActuator actuator, int count) {
        return new ActuatorContext(actuator, count);
    }

    public static ActuatorContext of(IActuator actuator) {
        return new ActuatorContext(actuator, -1);
    }

    public IActuator actuator() {
        return actuator;
    }

    public Integer max_executions() {
        return max_executions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ActuatorContext) obj;
        return Objects.equals(this.actuator, that.actuator) &&
                Objects.equals(this.max_executions, that.max_executions);
    }

}
