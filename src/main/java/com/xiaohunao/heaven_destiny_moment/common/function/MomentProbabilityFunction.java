package com.xiaohunao.heaven_destiny_moment.common.function;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import net.minecraft.world.level.Level;


@FunctionalInterface
public interface MomentProbabilityFunction {
    double getProbability(Level level);
}
