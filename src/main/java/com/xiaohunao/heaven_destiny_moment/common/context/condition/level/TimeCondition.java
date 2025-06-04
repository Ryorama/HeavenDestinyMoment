package com.xiaohunao.heaven_destiny_moment.common.context.condition.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record TimeCondition(Optional<Long> min, Optional<Long> max) implements ICondition {
    public static final ResourceLocation ID = HeavenDestinyMoment.asResource("time");
    public static final MapCodec<TimeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.LONG.optionalFieldOf("min").forGetter(TimeCondition::min),
        Codec.LONG.optionalFieldOf("max").forGetter(TimeCondition::max)
    ).apply(instance, TimeCondition::new));

    public static TimeCondition exactly(long value) {
        return new TimeCondition(Optional.of(value), Optional.of(value));
    }

    public static TimeCondition between(long min, long max) {
        return new TimeCondition(Optional.of(min), Optional.of(max));
    }

    public static TimeCondition atLeast(long min) {
        return new TimeCondition(Optional.of(min), Optional.empty());
    }

    public static TimeCondition atMost(long max) {
        return new TimeCondition(Optional.empty(), Optional.of(max));
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

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        Level level = instance.getLevel();
        return this.matches(level.getDayTime() % 24000);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.TIME_CONDITION.get();
    }

}
