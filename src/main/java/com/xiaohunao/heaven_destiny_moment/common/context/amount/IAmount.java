package com.xiaohunao.heaven_destiny_moment.common.context.amount;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import java.util.function.Function;

public  interface IAmount extends ICodec<IAmount> {
    Codec<IAmount> CODEC = Codec.lazyInitialized(HDMRegistries.AMOUNT_CODEC::byNameCodec).dispatch(IAmount::codec, Function.identity());

    int getAmount();

    MapCodec<? extends IAmount> codec();
}
