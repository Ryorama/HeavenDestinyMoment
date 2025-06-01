package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMTriggerTypes;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

public class LevelTickTrigger implements ITrigger {
    public static final LevelTickTrigger INSTANCE = new LevelTickTrigger();
    public static final MapCodec<LevelTickTrigger> CODEC = MapCodec.unit(INSTANCE);


    public boolean canTrigger() {
        return true;
    }
}
