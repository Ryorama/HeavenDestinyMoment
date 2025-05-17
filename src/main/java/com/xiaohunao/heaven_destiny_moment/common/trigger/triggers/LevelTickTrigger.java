package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LevelTickTrigger implements ITrigger {
    public static final LevelTickTrigger INSTANCE = new LevelTickTrigger();

    public boolean canTrigger() {
        return true;
    }
}
