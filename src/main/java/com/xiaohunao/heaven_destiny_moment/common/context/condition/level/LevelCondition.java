package com.xiaohunao.heaven_destiny_moment.common.context.condition.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.IBuilderConverter;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LevelCondition(Optional<DifficultyCondition> difficulty, Optional<TimeCondition> time, Optional<List<Integer>> validMoonPhases) implements ICondition {
    public static final MapCodec<LevelCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DifficultyCondition.CODEC.codec().optionalFieldOf("difficulty").forGetter(LevelCondition::difficulty),
            TimeCondition.CODEC.codec().optionalFieldOf("time").forGetter(LevelCondition::time),
            Codec.INT.listOf().optionalFieldOf("validMoonPhases").forGetter(LevelCondition::validMoonPhases)
    ).apply(instance, LevelCondition::new));
    @Override
    public boolean matches(AutomationContext context) {
        return matchesCondition(difficulty,context) &&
                matchesCondition(time,context) &&
                matchesValidMoonPhases(context.getLevel());
    }

    private boolean matchesCondition(Optional<? extends ICondition> condition, AutomationContext context) {
        return condition.map(cond -> cond.matches(context)).orElse(true);
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
        return CODEC;
    }

    public static class Builder implements IBuilderConverter<LevelCondition> {
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

        @Override
        public Builder converter(LevelCondition levelCondition) {
            Builder builder = new Builder();
            levelCondition.difficulty().ifPresent(diff -> builder.difficulty = diff);
            levelCondition.time().ifPresent(time -> builder.time = time);
            levelCondition.validMoonPhases().ifPresent(phases -> builder.validMoonPhases = new ArrayList<>(phases));
            return builder;
        }
    }
}
