package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.TimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.function.MomentProbabilityFunction;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;

public record TimeProbabilityTrigger(TimeCondition timeCondition, Optional<Double> base_probability, Optional<MomentProbabilityFunction> probabilityFunction) implements ITrigger {
    public static final MapCodec<TimeProbabilityTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TimeCondition.CODEC.fieldOf("time_condition").forGetter(TimeProbabilityTrigger::timeCondition),
            Codec.DOUBLE.optionalFieldOf("base_probability").forGetter(TimeProbabilityTrigger::base_probability),
            HDMRegistries.MOMENT_PROBABILITY_FUNCTION.byNameCodec().optionalFieldOf("probability_function").forGetter(TimeProbabilityTrigger::probabilityFunction)
    ).apply(instance, TimeProbabilityTrigger::new));

    private static final Random random = new Random();

    public static TimeProbabilityTrigger of(TimeCondition timeCondition, double probability) {
        return new TimeProbabilityTrigger(timeCondition, Optional.of(probability),Optional.empty());
    }

    public static TimeProbabilityTrigger of(TimeCondition timeCondition, MomentProbabilityFunction probabilityFunction) {
        return new TimeProbabilityTrigger(timeCondition, Optional.empty(),Optional.of(probabilityFunction));
    }


    @Override
    public boolean canTrigger(AutomationContext context) {
        if (context.getCurrentDayTime().isEmpty()) {
            return false;
        }
        boolean timeMatches = timeCondition.matches(context.getCurrentDayTime().get() % 24000);

        if (!timeMatches) {
            return false;
        }

        double nextFloat = random.nextDouble();
        if (probabilityFunction.isPresent()) {
            double modifiedProbability = probabilityFunction.get().getProbability(context.getLevel());
            return nextFloat < modifiedProbability;
        } else {
            if (base_probability.isPresent()){
                System.out.println(base_probability.get() + " " + nextFloat);
            }
            return base_probability.isPresent() && nextFloat < base_probability.get();
        }

    }


    public static TimeProbabilityTrigger exactly(long value, double probability) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(value), Optional.of(value)), Optional.of(probability), Optional.empty());
    }

    public static TimeProbabilityTrigger between(long min, long max, double probability) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(min), Optional.of(max)), Optional.of(probability), Optional.empty());
    }

    public static TimeProbabilityTrigger atLeast(long min, double probability) {
        return new TimeProbabilityTrigger(new TimeCondition(Optional.of(min), Optional.empty()), Optional.of(probability), Optional.empty());
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
