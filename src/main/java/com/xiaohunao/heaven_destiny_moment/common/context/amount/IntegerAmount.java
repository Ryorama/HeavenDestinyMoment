package com.xiaohunao.heaven_destiny_moment.common.context.amount;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;


public record IntegerAmount(int amount) implements IAmount {
    public static final MapCodec<IntegerAmount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("amount").forGetter(IntegerAmount::amount)
    ).apply(instance, IntegerAmount::new));

    @Override
    public int getAmount() {
        return amount;
    }
    @Override
    public MapCodec<? extends IAmount> codec() {
        return CODEC;
    }
}
