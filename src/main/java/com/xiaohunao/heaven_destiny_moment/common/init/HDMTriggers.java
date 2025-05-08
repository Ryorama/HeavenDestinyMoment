package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.LevelTickTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.RandomLevelTickTrigger;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;

public class HDMTriggers {
    public static final FlexibleRegister<ITrigger> TRIGGER = FlexibleRegister.create(HDMRegistries.TRIGGER, HeavenDestinyMoment.MODID);
}
