package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RandomLevelTickTrigger implements ISerializableTrigger {
    public static final MapCodec<RandomLevelTickTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("probability").forGetter(RandomLevelTickTrigger::getProbability)
            ).apply(instance, RandomLevelTickTrigger::new)
    );


   private final float Probability;

    public RandomLevelTickTrigger(float probability) {
        Probability = probability;
    }

    public float getProbability() {
        return Probability;
    }

    @Override
    public boolean canTrigger(MomentInstance momentInstance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return momentInstance.getLevel().getRandom().nextFloat() > Probability;
    }
}