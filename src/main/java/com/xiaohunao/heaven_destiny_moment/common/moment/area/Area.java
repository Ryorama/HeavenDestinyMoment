package com.xiaohunao.heaven_destiny_moment.common.moment.area;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Function;

public interface Area extends ICodec<Area> {
    Codec<Area> CODEC = Codec.lazyInitialized(HDMRegistries.AREA_CODEC::byNameCodec).dispatch(Area::codec, Function.identity());

    MapCodec<? extends Area> codec();

    boolean matches(ServerLevel level, BlockPos pos);
}
