package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

public class LevelTickTrigger implements ITrigger {
    public static final LevelTickTrigger INSTANCE = new LevelTickTrigger();
    public static final MapCodec<LevelTickTrigger> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }

    @Override
    public boolean canTrigger(AutomationContext context) {
        return true;
    }
}
