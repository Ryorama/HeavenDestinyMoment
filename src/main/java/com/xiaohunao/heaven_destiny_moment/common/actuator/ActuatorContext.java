package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ActuatorContext(IActuator actuator,Integer count) {
    public static final Codec<ActuatorContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IActuator.CODEC.fieldOf("actuator").forGetter(ActuatorContext::actuator),
            Codec.INT.fieldOf("count").forGetter(ActuatorContext::count)
    ).apply(instance, ActuatorContext::new));

    public static ActuatorContext of(IActuator actuator, int count) {
        return new ActuatorContext(actuator, count);
    }

    public static ActuatorContext of(IActuator actuator) {
        return new ActuatorContext(actuator, -1);
    }
}
