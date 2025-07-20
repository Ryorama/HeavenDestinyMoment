package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

public class KillEntityTrigger implements ITrigger {
    public static final KillEntityTrigger INSTANCE = new KillEntityTrigger();
    public static final MapCodec<KillEntityTrigger> CODEC = MapCodec.unit(INSTANCE);


    public boolean canTrigger() {
        return true;
    }


}

