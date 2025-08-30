package com.xiaohunao.heaven_destiny_moment.common.context.amount;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;

import java.util.Random;

public record RandomAmount(int min, int max) implements IAmount {
    public static final MapCodec<RandomAmount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("min").forGetter(RandomAmount::min),
            Codec.INT.fieldOf("max").forGetter(RandomAmount::max)
    ).apply(instance, RandomAmount::new));

    private static final Random rand = new Random();

    public static RandomAmount of(int min, int max) {
        return new RandomAmount(min, max);
    }

    @Override
    public int getAmount() {
        int newMax = Math.max(this.max, this.min);
        return Math.max(0, rand.nextInt(newMax - this.min + 1) + this.min);
    }
    @Override
    public MapCodec<? extends IAmount> codec() {
        return CODEC;
    }
}