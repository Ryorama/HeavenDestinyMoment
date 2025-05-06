package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.world.level.Level;

public interface LevelTickTrigger extends ITrigger {
    void onLevelTick(MomentInstance momentInstance, Level level);
}
