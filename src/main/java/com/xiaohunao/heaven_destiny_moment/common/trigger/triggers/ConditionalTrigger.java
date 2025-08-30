package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ConditionalTrigger(List<ICondition> conditions) implements ITrigger {
    public static final MapCodec<ConditionalTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ICondition.CODEC.listOf().fieldOf("conditions").forGetter(ConditionalTrigger::conditions)
    ).apply(instance, ConditionalTrigger::new));


    @Override
    public boolean canTrigger(AutomationContext context) {
        return conditions.stream().allMatch(condition -> condition.matches(context));
    }

    public static ConditionalTrigger of(ICondition... conditions) {
        return new ConditionalTrigger(List.of(conditions));
    }

    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }


}
