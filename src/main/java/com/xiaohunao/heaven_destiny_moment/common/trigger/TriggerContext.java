package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;

import java.util.List;

public record TriggerContext(ITrigger trigger, List<ICondition> conditions) {
    public static final Codec<TriggerContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITrigger.CODEC.fieldOf("trigger").forGetter(TriggerContext::trigger),
            ICondition.CODEC.listOf().fieldOf("conditions").forGetter(TriggerContext::conditions)
    ).apply(instance, TriggerContext::new));


    public static TriggerContext of(ITrigger trigger, ICondition... conditions) {
        return new TriggerContext(trigger, List.of(conditions));
    }

    public static TriggerContext of(ITrigger trigger){
        return new TriggerContext(trigger, List.of());
    }
}
