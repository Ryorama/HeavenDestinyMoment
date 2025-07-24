package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.*;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;

public class HDMTriggerTypes {
    public static final FlexibleRegister<TriggerType<?>> TRIGGER_TYPE = FlexibleRegister.create(HDMRegistries.TRIGGER_TYPE, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<TriggerType<?>,TriggerType<LevelTickTrigger>> LEVEL_TICK = TRIGGER_TYPE.registerStatic("level_tick",() -> new TriggerType<>(LevelTickTrigger.class,LevelTickTrigger.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<KillEntityTrigger>> KILL_ANY_ENTITY = TRIGGER_TYPE.registerStatic("kill_entity", () -> new TriggerType<>(KillEntityTrigger.class, KillEntityTrigger.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<RandomLevelTickTrigger>> RANDOM_LEVEL_TICK = TRIGGER_TYPE.registerStatic("random_level_tick",() -> new TriggerType<>(RandomLevelTickTrigger.class,RandomLevelTickTrigger.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<TimeProbabilityTrigger>> TIME_PROBABILITY = TRIGGER_TYPE.registerStatic("time_probability_trigger",() -> new TriggerType<>(TimeProbabilityTrigger.class,TimeProbabilityTrigger.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<BlockBreakTrigger>> BLOCK_BREAK = TRIGGER_TYPE.registerStatic("block_break",() -> new TriggerType<>(BlockBreakTrigger.class,BlockBreakTrigger.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<ConditionalTrigger>> CONDITIONAL_TRIGGER = TRIGGER_TYPE.registerStatic("conditional_trigger",() -> new TriggerType<>(ConditionalTrigger.class, ConditionalTrigger.CODEC));
}
