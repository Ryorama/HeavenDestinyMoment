package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockBreakTrigger extends ITrigger {

    boolean onBlockBreak(Level level, BlockPos pos, BlockState state, Player player);
} 