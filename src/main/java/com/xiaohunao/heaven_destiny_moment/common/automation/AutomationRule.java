package com.xiaohunao.heaven_destiny_moment.common.automation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceBuilder;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record AutomationRule(ResourceLocation name, IActuator actuator, IAmount maxExecutionCount, Optional<ITrigger> trigger, Optional<List<ICondition>> conditions) {
    public static final Codec<AutomationRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(AutomationRule::name),
            IActuator.CODEC.fieldOf("actuator").forGetter(AutomationRule::actuator),
            IAmount.CODEC.fieldOf("max_execution_count").forGetter(AutomationRule::maxExecutionCount),
            ITrigger.CODEC.optionalFieldOf("trigger_type").forGetter(AutomationRule::trigger),
            Codec.list(ICondition.CODEC).optionalFieldOf("conditions").forGetter(AutomationRule::conditions)
    ).apply(instance, AutomationRule::new));

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount,ITrigger trigger, List<ICondition> conditions) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.ofNullable(trigger), Optional.ofNullable(conditions));
    }

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount,ITrigger trigger, ICondition... conditions) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.ofNullable(trigger), Optional.of(List.of(conditions)));
    }

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount, ICondition... conditions) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.empty(), Optional.of(List.of(conditions)));
    }

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount,List<ICondition> conditions) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.empty(), Optional.ofNullable(conditions));
    }

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount, ITrigger trigger) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.ofNullable(trigger), Optional.empty());
    }

    public static AutomationRule of(ResourceLocation name,IActuator actuator,IAmount maxExecutionCount) {
        return new AutomationRule(name,actuator,maxExecutionCount, Optional.empty(), Optional.empty());
    }


    public boolean canRun(AutomationContext context) {
        return trigger.map(trigger -> trigger.canTrigger(context)).orElse(true) &&
                conditions.map(conditions -> conditions.stream().allMatch(condition -> condition.matches(context))).orElse(true);
    }
}