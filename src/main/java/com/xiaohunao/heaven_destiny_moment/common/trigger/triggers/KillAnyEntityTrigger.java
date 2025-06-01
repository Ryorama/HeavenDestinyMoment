package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

public abstract class KillAnyEntityTrigger implements ITrigger {
    public boolean canTrigger() {
        return true;
    }

    public static class Common extends KillAnyEntityTrigger {
        public static final KillAnyEntityTrigger.Common INSTANCE = new KillAnyEntityTrigger.Common();
        public static final MapCodec<KillAnyEntityTrigger.Common> CODEC = MapCodec.unit(INSTANCE);
    }

    public static class Moment extends KillAnyEntityTrigger {
        public static final KillAnyEntityTrigger.Moment INSTANCE = new KillAnyEntityTrigger.Moment();
        public static final MapCodec<KillAnyEntityTrigger.Moment> CODEC = MapCodec.unit(INSTANCE);
    }
}

