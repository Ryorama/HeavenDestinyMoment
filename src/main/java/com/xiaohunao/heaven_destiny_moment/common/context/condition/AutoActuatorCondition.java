package com.xiaohunao.heaven_destiny_moment.common.context.condition;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import net.minecraft.resources.ResourceLocation;

public record AutoActuatorCondition(ResourceLocation autoActuatorName) implements ICondition{
    public static final MapCodec<AutoActuatorCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("auto_actuator_name").forGetter(AutoActuatorCondition::autoActuatorName)
    ).apply(instance, AutoActuatorCondition::new));

    @Override
    public boolean matches(AutomationContext context) {
       return context.getMomentInstance().map(momentInstance -> {
            Pair<AutomationRule, Integer> runtimeAutoActuator = momentInstance.getTriggerManager().getRuntimeAutoActuator(autoActuatorName);
            return runtimeAutoActuator != null && runtimeAutoActuator.getSecond() != 0;
        }).orElse(false);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
