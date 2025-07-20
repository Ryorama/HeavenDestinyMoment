package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import net.minecraft.world.Difficulty;

import java.util.function.BiFunction;

/**
 * 提供各种缩放算法的函数
 */
public class HDMScalingFunctions {
    public static final FlexibleRegister<BiFunction<Integer, Difficulty, Integer>> DIFFICULTY_SCALING = 
            FlexibleRegister.create(HDMRegistries.DIFFICULTY_SCALING, HeavenDestinyMoment.MODID);

    public static final FlexibleRegister<BiFunction<Integer, Integer, Integer>> PLAYER_COUNT_SCALING = 
            FlexibleRegister.create(HDMRegistries.PLAYER_COUNT_SCALING, HeavenDestinyMoment.MODID);
    

    public static final FlexibleHolder<BiFunction<Integer, Difficulty, Integer>, ?> COMMON =
            DIFFICULTY_SCALING.registerStatic("common", () ->
                (baseValue, difficulty) -> {
                    float modifier = switch (difficulty) {
                        case PEACEFUL -> 0.5f;
                        case EASY -> 1.0f;
                        case NORMAL -> 1.5f;
                        case HARD -> 2.0f;
                    };
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<BiFunction<Integer, Difficulty, Integer>, ?> EASY =
            DIFFICULTY_SCALING.registerStatic("common", () ->
                    (baseValue, difficulty) -> {
                        float modifier = switch (difficulty) {
                            case PEACEFUL -> 0.25f;
                            case EASY -> 0.5f;
                            case NORMAL -> 0.75f;
                            case HARD -> 1.0f;
                        };
                        return Math.round(baseValue * modifier);
                    }
            );

    public static final FlexibleHolder<BiFunction<Integer, Difficulty, Integer>, ?> HARD =
            DIFFICULTY_SCALING.registerStatic("hard", () ->
                (baseValue, difficulty) -> {
                    float modifier = switch (difficulty) {
                        case PEACEFUL -> 1.0f;
                        case EASY -> 2.0f;
                        case NORMAL -> 4.0f;
                        case HARD -> 8.0f;
                    };
                    return Math.round(baseValue + modifier);
                }
            );


    public static final FlexibleHolder<BiFunction<Integer, Integer, Integer>, ?> LINEAR =
            PLAYER_COUNT_SCALING.registerStatic("linear", () ->
                (baseValue, playerCount) -> {
                    int count = Math.max(1, playerCount);
                    float modifier = 1.0f + (count - 1) * 0.5f;
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<BiFunction<Integer, Integer, Integer>, ?> SQRT =
            PLAYER_COUNT_SCALING.registerStatic("sqrt", () ->
                (baseValue, playerCount) -> {
                    int count = Math.max(1, playerCount);
                    float modifier = (float)Math.sqrt(count);
                    return Math.round(baseValue * modifier);
                }
            );


    public static final FlexibleHolder<BiFunction<Integer, Integer, Integer>, ?> LOGARITHMIC =
            PLAYER_COUNT_SCALING.registerStatic("logarithmic", () ->
                (baseValue, playerCount) -> {
                    int count = Math.max(1, playerCount);
                    float modifier = 1.0f + (float)Math.log(count);
                    return Math.round(baseValue * modifier);
                }
            );

    public static final FlexibleHolder<BiFunction<Integer, Integer, Integer>, ?> MULTIPLY =
            PLAYER_COUNT_SCALING.registerStatic("multiply", () ->
                    (baseValue, playerCount) -> {
                        int count = Math.max(1, playerCount);
                        return baseValue * count;
                    }
            );
}
