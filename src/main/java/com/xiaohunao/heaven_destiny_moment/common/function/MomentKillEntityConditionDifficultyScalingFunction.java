package com.xiaohunao.heaven_destiny_moment.common.function;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.Difficulty;

@FunctionalInterface
public interface MomentKillEntityConditionDifficultyScalingFunction {
    int scale(int baseValue, MomentInstance momentInstance);
}
