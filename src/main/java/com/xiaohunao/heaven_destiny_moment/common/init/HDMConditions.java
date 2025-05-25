package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.*;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.AutoProbabilityCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.WorldUniqueMomentCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.DifficultyCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.TimeCondition;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;

public class HDMConditions {
    public static final FlexibleRegister<MapCodec<? extends ICondition>> CONDITION_CODEC = FlexibleRegister.create(HDMRegistries.CONDITION_CODEC, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> TIME_CONDITION = CONDITION_CODEC.registerStatic("time", () -> TimeCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> LOCATION_CONDITION = CONDITION_CODEC.registerStatic("location", () -> LocationCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> WORLD_UNIQUE_MOMENT = CONDITION_CODEC.registerStatic("world_unique_moment", () -> WorldUniqueMomentCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> AUTO_PROBABILITY = CONDITION_CODEC.registerStatic("auto_probability", () -> AutoProbabilityCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> DIFFICULTY = CONDITION_CODEC.registerStatic("difficulty", () -> DifficultyCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> LEVEL = CONDITION_CODEC.registerStatic("level", () -> LevelCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> KILL_ENTITY_CONDITION = CONDITION_CODEC.registerStatic("kill_entity", () -> KillEntityCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> PLAYER_CONDITION = CONDITION_CODEC.registerStatic("player", () -> PlayerCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> MOD_LOADED_CONDITION = CONDITION_CODEC.registerStatic("mod_loaded", () -> ModLoadedCondition.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> OR_CONDITION = CONDITION_CODEC.registerStatic("or", () -> OrCondition.CODEC);

}
