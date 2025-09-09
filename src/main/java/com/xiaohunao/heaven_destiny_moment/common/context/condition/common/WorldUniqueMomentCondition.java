package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class WorldUniqueMomentCondition implements ICondition {
    public static final WorldUniqueMomentCondition DEFAULT = new WorldUniqueMomentCondition();
    public static MapCodec<WorldUniqueMomentCondition> CODEC = MapCodec.unit(DEFAULT);

    @Override
    public boolean matches(AutomationContext context) {
        if (context.momentInstance().isPresent()) {
            Level level = context.getLevel();
            MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
            Collection<MomentInstance> momentInstances = momentInstanceManager.getMomentInstances(context.momentInstance().get().getType());
            return momentInstances.isEmpty();
        }
        return  false;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
