package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;

import java.util.function.Function;

public interface ITrigger extends ICodec<ITrigger> {
    Codec<ITrigger> CODEC = Codec.lazyInitialized(HDMRegistries.TRIGGER_CODEC::byNameCodec).dispatch(ITrigger::codec, Function.identity());

    MapCodec<? extends ITrigger> codec();

    boolean canTrigger(AutomationContext context);
}
