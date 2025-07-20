package com.xiaohunao.heaven_destiny_moment.common.context.condition.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.LocationCondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public record LevelCondition(Optional<DifficultyCondition> difficulty, Optional<TimeCondition> time, Optional<List<Integer>> validMoonPhases) implements ICondition {
    public static final MapCodec<LevelCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DifficultyCondition.CODEC.codec().optionalFieldOf("difficulty").forGetter(LevelCondition::difficulty),
            TimeCondition.CODEC.codec().optionalFieldOf("time").forGetter(LevelCondition::time),
            Codec.INT.listOf().optionalFieldOf("validMoonPhases").forGetter(LevelCondition::validMoonPhases)
    ).apply(instance, LevelCondition::new));
    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return matchesCondition(difficulty,instance, pos, serverPlayer) &&
                matchesCondition(time,instance, pos, serverPlayer) &&
                matchesValidMoonPhases(instance.getLevel());
    }

    private boolean matchesCondition(Optional<? extends ICondition> condition, MomentInstance instance, BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return condition.map(cond -> cond.matches(instance, pos, serverPlayer)).orElse(true);
    }
    private boolean matchesValidMoonPhases(Level level) {
        return validMoonPhases.map(s -> s.contains(level.getMoonPhase())).orElse(true);
    }


    public static LevelCondition validMoonPhases(Integer... validMoonPhases) {
        return Builder.level().validMoonPhases(validMoonPhases).build();
    }

    public static LevelCondition difficulty(Difficulty difficulty) {
        return Builder.level().difficulty(difficulty).build();
    }




    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.LEVEL.get();
    }

    public static class Builder {
        private DifficultyCondition difficulty;
        private TimeCondition time;
        private List<Integer> validMoonPhases;

        public static Builder level() {
            return new LevelCondition.Builder();
        }

        public Builder difficulty(Difficulty difficulty) {
            this.difficulty = DifficultyCondition.of(difficulty);
            return this;
        }
        public Builder time(TimeCondition time) {
            this.time = time;
            return this;
        }

        public Builder validMoonPhases(Integer... validMoonPhases) {
            this.validMoonPhases = List.of(validMoonPhases);
            return this;
        }

        public LevelCondition build() {
            return new LevelCondition(Optional.ofNullable(difficulty), Optional.ofNullable(time),Optional.ofNullable(validMoonPhases));
        }
    }
}
