package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.ConditionalTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record AutoActuatorGroupSettings(Map<TriggerContext,ActuatorContext> autoActuators) {
    public static final Codec<AutoActuatorGroupSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.pair(TriggerContext.CODEC, ActuatorContext.CODEC)
                    .listOf()
                    .fieldOf("autoActuators")
                    .forGetter(settings -> settings.autoActuators.entrySet().stream()
                            .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList()))
    ).apply(instance, entries -> {
        Map<TriggerContext, ActuatorContext> map = new HashMap<>();
        entries.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));
        return new AutoActuatorGroupSettings(map);
    }));


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private final Map<TriggerContext, ActuatorContext> autoActuators = Maps.newHashMap();

        public AutoActuatorGroupSettings build() {
            return new AutoActuatorGroupSettings(autoActuators);
        }

        public Builder state(MomentState state, ITrigger trigger, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(trigger, conditions), ActuatorContext.of(StateSettingActuator.of(state),1));
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger, IActuator actuator) {
            autoActuators.put(TriggerContext.of(trigger), ActuatorContext.of(actuator,1));
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger) {
            autoActuators.put(TriggerContext.of(trigger), ActuatorContext.of(StateSettingActuator.of(state),1));
            return this;
        }

        public Builder state(MomentState state, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(new ConditionalTrigger(List.of(conditions))), ActuatorContext.of(StateSettingActuator.of(state),1));
            return this;
        }


        public Builder actuator(IActuator actuator, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(new ConditionalTrigger(List.of(conditions))), ActuatorContext.of(actuator,1));
            return this;
        }

        public Builder actuator(IActuator actuator, ITrigger trigger, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(trigger, conditions), ActuatorContext.of(actuator,1));
            return this;
        }

        public Builder actuator(IActuator actuator, ITrigger trigger) {
            autoActuators.put(TriggerContext.of(trigger), ActuatorContext.of(actuator,1));
            return this;
        }

        public Builder actuator(IActuator actuator, int executionCount, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(new ConditionalTrigger(List.of(conditions))), ActuatorContext.of(actuator,executionCount));
            return this;
        }

        public Builder actuator(IActuator actuator,int executionCount, ITrigger trigger, ICondition... conditions) {
            autoActuators.put(TriggerContext.of(trigger, conditions), ActuatorContext.of(actuator,executionCount));
            return this;
        }

        public Builder actuator(IActuator actuator,int executionCount, ITrigger trigger) {
            autoActuators.put(TriggerContext.of(trigger), ActuatorContext.of(actuator,executionCount));
            return this;
        }

    }
}
