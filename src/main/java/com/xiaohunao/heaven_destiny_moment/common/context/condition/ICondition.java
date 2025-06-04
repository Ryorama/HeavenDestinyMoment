package com.xiaohunao.heaven_destiny_moment.common.context.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ICondition {
    Codec<ICondition> CODEC = Codec.lazyInitialized(HDMRegistries.CONDITION_CODEC::byNameCodec).dispatch(ICondition::codec, Function.identity());

    boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer);

    MapCodec<? extends ICondition> codec();
}