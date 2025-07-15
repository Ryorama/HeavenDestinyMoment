package com.xiaohunao.heaven_destiny_moment.common.context.condition.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentHistoryManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentRunningRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record MomentRunningTimeCondition(Optional<Long> min, Optional<Long> max) implements ICondition {
    public static final MapCodec<MomentRunningTimeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.LONG.optionalFieldOf("min").forGetter(MomentRunningTimeCondition::min),
            Codec.LONG.optionalFieldOf("max").forGetter(MomentRunningTimeCondition::max)
    ).apply(instance, MomentRunningTimeCondition::new));

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        Level level = instance.getLevel();
        MomentRunningRecord activeRecords = MomentHistoryManager.of(level).getActiveRecords(instance.getType(), instance.getID());
        if (activeRecords != null){
            long runningTime = level.getGameTime() - activeRecords.getCreateTime();
            return this.matches(runningTime);
        }
        return false;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.MOMENT_RUNNING_TIME.get();
    }

    public static MomentRunningTimeCondition exactly(long value) {
        return new MomentRunningTimeCondition(Optional.of(value), Optional.of(value));
    }

    public static MomentRunningTimeCondition between(long min, long max) {
        return new MomentRunningTimeCondition(Optional.of(min), Optional.of(max));
    }

    public static MomentRunningTimeCondition atLeast(long min) {
        return new MomentRunningTimeCondition(Optional.of(min), Optional.empty());
    }

    public static MomentRunningTimeCondition atMost(long max) {
        return new MomentRunningTimeCondition(Optional.empty(), Optional.of(max));
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
