package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;

import java.util.List;

public record ConditionalTrigger(ITrigger trigger, List<ICondition> conditions) {
    public static final Codec<ConditionalTrigger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITrigger.CODEC.fieldOf("trigger").forGetter(ConditionalTrigger::trigger),
            ICondition.CODEC.listOf().fieldOf("conditions").forGetter(ConditionalTrigger::conditions)
    ).apply(instance, ConditionalTrigger::new));
}
