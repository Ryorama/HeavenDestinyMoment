package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import java.util.UUID;
import java.util.function.Function;

public interface ITracker extends ICodec<ITracker> {
    Codec<ITracker> CODEC = Codec.lazyInitialized(HDMRegistries.TRACKER_CODEC::byNameCodec).dispatch(ITracker::codec, Function.identity());


    MapCodec<? extends ITracker> codec();
}
