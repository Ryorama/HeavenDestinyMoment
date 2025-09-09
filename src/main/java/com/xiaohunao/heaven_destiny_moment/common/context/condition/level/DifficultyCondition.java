package com.xiaohunao.heaven_destiny_moment.common.context.condition.level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.Nullable;

public record DifficultyCondition(Difficulty difficulty) implements ICondition {
    public static final DifficultyCondition PEACEFUL = new DifficultyCondition(Difficulty.PEACEFUL);
    public static final DifficultyCondition EASY = new DifficultyCondition(Difficulty.EASY);
    public static final DifficultyCondition NORMAL = new DifficultyCondition(Difficulty.NORMAL);
    public static final DifficultyCondition HARD = new DifficultyCondition(Difficulty.HARD);

    public static final MapCodec<DifficultyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Difficulty.CODEC.fieldOf("difficulty").forGetter(DifficultyCondition::difficulty)
    ).apply(instance, DifficultyCondition::new));

    public static DifficultyCondition of(Difficulty difficulty) {
        return new DifficultyCondition(difficulty);
    }

    @Override
    public boolean matches(AutomationContext context) {
        return context.difficulty().isPresent() && context.difficulty().get() == difficulty;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
