package com.xiaohunao.heaven_destiny_moment.common.context.condition.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record LevelRunningTimeCondition(Optional<Long> min, Optional<Long> max) implements ICondition {
    public static final MapCodec<LevelRunningTimeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.optionalFieldOf("min").forGetter(LevelRunningTimeCondition::min),
            Codec.LONG.optionalFieldOf("max").forGetter(LevelRunningTimeCondition::max)
    ).apply(instance, LevelRunningTimeCondition::new));

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        Level level = instance.getLevel();
        return this.matches(level.getGameTime());
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public static LevelRunningTimeCondition exactly(long value) {
        return new LevelRunningTimeCondition(Optional.of(value), Optional.of(value));
    }

    public static LevelRunningTimeCondition between(long min, long max) {
        return new LevelRunningTimeCondition(Optional.of(min), Optional.of(max));
    }

    public static LevelRunningTimeCondition atLeast(long min) {
        return new LevelRunningTimeCondition(Optional.of(min), Optional.empty());
    }

    public static LevelRunningTimeCondition atMost(long max) {
        return new LevelRunningTimeCondition(Optional.empty(), Optional.of(max));
    }

    public boolean matches(long value) {
        Long minVal = min.orElse(null);
        Long maxVal = max.orElse(null);

        if (minVal != null && maxVal != null) {
            if (minVal <= maxVal) {
                return value >= minVal && value <= maxVal;
            } else {
                return value >= minVal || value <= maxVal;
            }
        }

        if (minVal != null) {
            return value >= minVal;
        }

        if (maxVal != null) {
            return value <= maxVal;
        }

        return false;
    }
}
