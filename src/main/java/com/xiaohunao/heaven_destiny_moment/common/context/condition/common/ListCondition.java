package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ListCondition(List<ICondition> conditions) implements ICondition {
    public static final MapCodec<ListCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(ICondition.CODEC).fieldOf("conditions").forGetter(ListCondition::conditions)
    ).apply(instance, ListCondition::new));

    public static ListCondition of(ICondition... conditions) {
        return new ListCondition(List.of(conditions));
    }

    @Override
    public boolean matches(AutomationContext automationContext) {
        return conditions.stream().allMatch(condition -> condition.matches(automationContext));
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
