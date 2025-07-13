package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

public abstract class KillEntityTrigger implements ITrigger {
    public boolean canTrigger() {
        return true;
    }

    public static class Common extends KillEntityTrigger {
        public static final KillEntityTrigger.Common INSTANCE = new KillEntityTrigger.Common();
        public static final MapCodec<KillEntityTrigger.Common> CODEC = MapCodec.unit(INSTANCE);
    }

    public static class Moment extends KillEntityTrigger {
        public static final KillEntityTrigger.Moment INSTANCE = new KillEntityTrigger.Moment();
        public static final MapCodec<KillEntityTrigger.Moment> CODEC = MapCodec.unit(INSTANCE);
    }
}

