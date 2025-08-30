package com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;


public interface ISpawnAlgorithm extends ICodec<ISpawnAlgorithm> {
    Codec<ISpawnAlgorithm> CODEC = Codec.lazyInitialized(HDMRegistries.SPAWN_ALGORITHM_CODEC::byNameCodec).dispatch(ISpawnAlgorithm::codec, Function.identity());

    Vec3 spawn(MomentInstance momentInstance, Entity entity);

    MapCodec<? extends ISpawnAlgorithm> codec();
}
