package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.function.MomentKillEntityConditionDifficultyScalingFunction;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

/**
 * 提供各种缩放算法的函数
 */
public class HDMScalingFunctions {
    public static final FlexibleRegister<MomentKillEntityConditionDifficultyScalingFunction> MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION =
            FlexibleRegister.create(HDMRegistries.MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION, HeavenDestinyMoment.MODID);


    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> DIFFICULTY_COMMON =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("difficulty_common", () ->
                (baseValue, momentInstance) -> {
                    Level level = momentInstance.getLevel();
                    Difficulty difficulty = level.getDifficulty();
                    float modifier = switch (difficulty) {
                        case PEACEFUL -> 0.5f;
                        case EASY -> 1.0f;
                        case NORMAL -> 1.5f;
                        case HARD -> 2.0f;
                    };
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> DIFFICULTY_EASY =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("difficulty_common", () ->
                    (baseValue, momentInstance) -> {
                        Level level = momentInstance.getLevel();
                        Difficulty difficulty = level.getDifficulty();
                        float modifier = switch (difficulty) {
                            case PEACEFUL -> 0.25f;
                            case EASY -> 0.5f;
                            case NORMAL -> 0.75f;
                            case HARD -> 1.0f;
                        };
                        return Math.round(baseValue * modifier);
                    }
            );

    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> DIFFICULTY_HARD =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("difficulty_hard", () ->
                (baseValue, momentInstance) -> {
                    Level level = momentInstance.getLevel();
                    Difficulty difficulty = level.getDifficulty();
                    float modifier = switch (difficulty) {
                        case PEACEFUL -> 1.0f;
                        case EASY -> 2.0f;
                        case NORMAL -> 4.0f;
                        case HARD -> 8.0f;
                    };
                    return Math.round(baseValue + modifier);
                }
            );


    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> PLAYER_COUNT_LINEAR =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("player_count_linear", () ->
                (baseValue, momentInstance) -> {
                    int playerCount = momentInstance.getPlayers().size();
                    int count = Math.max(1, playerCount);
                    float modifier = 1.0f + (count - 1) * 0.5f;
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> PLAYER_COUNT_SQRT =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("player_count_sqrt", () ->
                (baseValue, momentInstance) -> {
                    int playerCount = momentInstance.getPlayers().size();
                    int count = Math.max(1, playerCount);
                    float modifier = (float)Math.sqrt(count);
                    return Math.round(baseValue * modifier);
                }
            );


    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> PLAYER_COUNT_LOGARITHMIC =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("player_count_logarithmic", () ->
                (baseValue, momentInstance) -> {
                    int playerCount = momentInstance.getPlayers().size();
                    int count = Math.max(1, playerCount);
                    float modifier = 1.0f + (float)Math.log(count);
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<MomentKillEntityConditionDifficultyScalingFunction, ?> PLAYER_COUNT_MULTIPLY =
            MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION.registerStatic("player_count_multiply", () ->
                    (baseValue, momentInstance) -> {
                        int playerCount = momentInstance.getPlayers().size();
                        int count = Math.max(1, playerCount);
                        return baseValue * count;
                    }
            );
}
