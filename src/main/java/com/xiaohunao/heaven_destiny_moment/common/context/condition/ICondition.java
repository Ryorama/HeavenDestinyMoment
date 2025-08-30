package com.xiaohunao.heaven_destiny_moment.common.context.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import java.util.function.Function;

public interface ICondition extends ICodec<ICondition> {
    Codec<ICondition> CODEC = Codec.lazyInitialized(HDMRegistries.CONDITION_CODEC::byNameCodec).dispatch(ICondition::codec, Function.identity());
    boolean matches(AutomationContext context);

    MapCodec<? extends ICondition> codec();
}