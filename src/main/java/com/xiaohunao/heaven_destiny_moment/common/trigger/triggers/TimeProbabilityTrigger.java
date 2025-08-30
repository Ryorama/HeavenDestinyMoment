package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.TimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Random;

public record TimeProbabilityTrigger(TimeCondition timeCondition, float probability) implements ITrigger {
    public static final MapCodec<TimeProbabilityTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TimeCondition.CODEC.fieldOf("time_condition").forGetter(TimeProbabilityTrigger::timeCondition),
            Codec.FLOAT.fieldOf("probability").forGetter(TimeProbabilityTrigger::probability)
    ).apply(instance, TimeProbabilityTrigger::new));

    private static final Random random = new Random();

    public static TimeProbabilityTrigger of(TimeCondition timeCondition, float probability) {
        return new TimeProbabilityTrigger(timeCondition, probability);
    }

    public boolean canTrigger(Level level) {
        boolean timeMatches = timeCondition.matches(level.getDayTime() % 24000);

        if (!timeMatches) {
            return false;
        }
        float nextFloat = random.nextFloat();
        return nextFloat < probability;
    }

    public static TimeProbabilityTrigger atMost(long max, double probability) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.empty(), Optional.of(max)), Optional.of(probability), Optional.empty());
    }



    public static TimeProbabilityTrigger exactly(long value, MomentProbabilityFunction probabilityFunction) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(value), Optional.of(value)), Optional.empty(), Optional.of(probabilityFunction));
    }

    public static TimeProbabilityTrigger between(long min, long max, MomentProbabilityFunction probabilityFunction) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(min), Optional.of(max)), Optional.empty(), Optional.of(probabilityFunction));
    }

    public static TimeProbabilityTrigger atLeast(long min, MomentProbabilityFunction probabilityFunction) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(min), Optional.empty()), Optional.empty(), Optional.of(probabilityFunction));
    }

    public static TimeProbabilityTrigger atMost(long max, MomentProbabilityFunction probabilityFunction) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.empty(), Optional.of(max)), Optional.empty(), Optional.of(probabilityFunction));
    }



    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }
}
