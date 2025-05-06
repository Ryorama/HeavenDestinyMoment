package com.xiaohunao.heaven_destiny_moment.common.callback.callback;

import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ConditionCallback extends CallbackSerializable {
    boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer);
}
