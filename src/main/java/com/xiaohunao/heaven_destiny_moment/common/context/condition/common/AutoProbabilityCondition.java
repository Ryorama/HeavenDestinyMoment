package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public record AutoProbabilityCondition(int probability) implements ICondition {
    public static final MapCodec<AutoProbabilityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("probability").forGetter(AutoProbabilityCondition::probability)
    ).apply(instance, AutoProbabilityCondition::new));

    static Random random = new Random();
    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return random.nextInt(probability) == 0;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMContextRegister.AUTO_PROBABILITY.get();
    }
}
