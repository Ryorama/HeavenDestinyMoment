package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IntegerAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.ConditionalTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record AutoActuatorGroupSettings(Optional<AutomationRule> createRule, Optional<List<AutomationRule>> runtimeRules) {
    public static final Codec<AutoActuatorGroupSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AutomationRule.CODEC.optionalFieldOf("createRule").forGetter(AutoActuatorGroupSettings::createRule),
            Codec.list(AutomationRule.CODEC).optionalFieldOf("runtimeRules").forGetter(AutoActuatorGroupSettings::runtimeRules)
    ).apply(instance, AutoActuatorGroupSettings::new));


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder implements IBuilderConverter<AutoActuatorGroupSettings> {
        private AutomationRule createRule = null;
        private final List<AutomationRule> runtimeRules = Lists.newArrayList();

        public AutoActuatorGroupSettings build() {
            return new AutoActuatorGroupSettings(Optional.ofNullable(createRule),Optional.of(runtimeRules));
        }

        @Override
        public Builder converter(AutoActuatorGroupSettings autoActuatorGroupSettings) {
            Builder builder = new Builder();
            autoActuatorGroupSettings.createRule.ifPresent(rule -> builder.createRule = rule);
            autoActuatorGroupSettings.runtimeRules.ifPresent(builder.runtimeRules::addAll);
            return builder;
        }

        public Builder create(ResourceLocation name, ITrigger trigger, ICondition... conditions) {
            createRule = AutomationRule.of(
                    name,
                    new CreateMomentInstanceActuator(),
                    IntegerAmount.of(1),
                    trigger,
                    conditions)
            ;
            return this;
        }

        public Builder create(ResourceLocation name,ITrigger trigger) {
            createRule = AutomationRule.of(
                    name,
                    new CreateMomentInstanceActuator(),
                    IntegerAmount.of(1),
                    trigger
            );
            return this;
        }

        public Builder create(ResourceLocation name,ICondition... conditions) {
            createRule = AutomationRule.of(
                    name,
                    new CreateMomentInstanceActuator(),
                    IntegerAmount.of(1),
                    List.of(conditions)
            );
            return this;
        }


        public Builder state(ResourceLocation name,MomentState state, ITrigger trigger, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    StateSettingActuator.of(state),
                    IntegerAmount.of(1),
                    trigger,
                    List.of(conditions)
            ));
            return this;
        }

        public Builder state(ResourceLocation name,MomentState state, ITrigger trigger) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    StateSettingActuator.of(state),
                    IntegerAmount.of(1),
                    trigger
            ));
            return this;
        }
        public Builder state(ResourceLocation name,MomentState state, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    StateSettingActuator.of(state),
                    IntegerAmount.of(1),
                    List.of(conditions)
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name,IActuator actuator, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    IntegerAmount.of(1),
                    new ConditionalTrigger(List.of(conditions)),
                    List.of(conditions)
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name,IActuator actuator, ITrigger trigger, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    IntegerAmount.of(1),
                    trigger,
                    List.of(conditions)
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name,IActuator actuator, ITrigger trigger) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    IntegerAmount.of(1),
                    trigger,
                    List.of()
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name, IActuator actuator, IAmount executionCount, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    executionCount,
                    new ConditionalTrigger(List.of(conditions)),
                    List.of(conditions)
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name,IActuator actuator,IAmount executionCount, ITrigger trigger, ICondition... conditions) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    executionCount,
                    trigger,
                    List.of(conditions)
            ));
            return this;
        }

        public Builder actuator(ResourceLocation name,IActuator actuator,IAmount executionCount, ITrigger trigger) {
            runtimeRules.add(AutomationRule.of(
                    name,
                    actuator,
                    executionCount,
                    trigger,
                    List.of()
            ));
            return this;
        }

        public Builder remove(ResourceLocation name) {
            runtimeRules.removeIf(rule -> rule.name().equals(name));
            return this;
        }

    }
}
