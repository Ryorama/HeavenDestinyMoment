package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record InvertCondition(ICondition condition) implements ICondition{
    public static final MapCodec<InvertCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ICondition.CODEC.fieldOf("invert").forGetter(InvertCondition::condition)
    ).apply(instance, InvertCondition::new));

    public static InvertCondition of(ICondition condition) {
        return new InvertCondition(condition);
    }

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return !condition.matches(instance, pos, serverPlayer);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
