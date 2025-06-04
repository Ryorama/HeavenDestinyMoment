package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class WorldUniqueMomentCondition implements ICondition {
    public static final WorldUniqueMomentCondition DEFAULT = new WorldUniqueMomentCondition();
    public static MapCodec<WorldUniqueMomentCondition> CODEC = MapCodec.unit(DEFAULT);

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(instance.getLevel());
        for (MomentInstance momentInstance : momentInstanceManager.getMomentInstances()) {
            if (momentInstance.getClass() == instance.getClass()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.WORLD_UNIQUE_MOMENT.get();
    }
}
