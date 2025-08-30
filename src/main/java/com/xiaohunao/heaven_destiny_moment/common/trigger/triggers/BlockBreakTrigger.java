package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public record BlockBreakTrigger(Block block) implements ITrigger {
    public static final MapCodec<BlockBreakTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockBreakTrigger::block)
            ).apply(instance, BlockBreakTrigger::new)
    );

    public static BlockBreakTrigger of(Block block) {
        return new BlockBreakTrigger(block);
    }

    public boolean canTrigger(Level level, BlockPos pos) {
        if (pos == null) return false;
        return level.getBlockState(pos).getBlock() == block;
    }

    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }


}