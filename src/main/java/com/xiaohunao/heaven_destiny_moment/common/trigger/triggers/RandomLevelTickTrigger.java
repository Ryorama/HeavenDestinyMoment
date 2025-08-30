package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.world.level.Level;

public class RandomLevelTickTrigger implements ITrigger {
    public static final MapCodec<RandomLevelTickTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("probability").forGetter(RandomLevelTickTrigger::getProbability)
            ).apply(instance, RandomLevelTickTrigger::new)
    );


   private final float Probability;

    private RandomLevelTickTrigger(float probability) {
        Probability = probability;
    }

    public static RandomLevelTickTrigger of(float probability) {
        return new RandomLevelTickTrigger(probability);
    }

    public float getProbability() {
        return Probability;
    }

    public boolean canTrigger(Level level) {
        return level.getRandom().nextFloat() > Probability;
    }

    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }

    @Override
    public boolean canTrigger(AutomationContext context) {
        if (context.getLevel() == null) {
            return false;
        }

        return context.getLevel().getRandom().nextFloat() > Probability;
    }
}