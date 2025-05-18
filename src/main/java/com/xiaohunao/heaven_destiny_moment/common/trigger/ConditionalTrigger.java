package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ConditionalTrigger(List<ICondition> conditions) implements ISerializableTrigger {
    public static final Codec<ConditionalTrigger> CODEC = ICondition.CODEC.listOf().xmap(ConditionalTrigger::new, ConditionalTrigger::conditions);

    public boolean canTrigger(MomentInstance instance, @Nullable MomentState tryModifyState, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        for (ICondition condition : conditions) {
            if (!condition.matches(instance, tryModifyState, pos, serverPlayer)) {
                return false;
            }
        }
        return true;
    }

    public static ConditionalTrigger of(ICondition... conditions) {
        return new ConditionalTrigger(List.of(conditions));
    }
}
