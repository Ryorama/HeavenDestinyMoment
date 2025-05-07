package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.BlockBreakTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.LevelTickTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.RandomLevelTickTrigger;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HDMTriggerTypes {
    public static final FlexibleRegister<TriggerType<?>> TRIGGER_TYPE = FlexibleRegister.create(HDMRegistries.TRIGGER_TYPE, HeavenDestinyMoment.MODID, TriggerTypeManager.getInstance());

    public static final FlexibleHolder<TriggerType<?>,TriggerType<LevelTickTrigger>> LEVEL_TICK = register("level_tick",LevelTickTrigger.class);
    public static final FlexibleHolder<TriggerType<?>,TriggerType<RandomLevelTickTrigger>> RANDOM_LEVEL_TICK = registerSerializable("random_level_tick",RandomLevelTickTrigger.class,RandomLevelTickTrigger.CODEC);


    private static <T extends ITrigger> FlexibleHolder<TriggerType<?>, TriggerType<T>> register(String id, Class<T> hookClass) {
        return TRIGGER_TYPE.registerStatic(id, () -> TriggerType.createHook(HeavenDestinyMoment.asResource(id), hookClass));
    }

    private static <T extends ISerializableTrigger> FlexibleHolder<TriggerType<?>, TriggerType<T>> registerSerializable(String id, Class<T> hookClass, MapCodec<T> codec) {
        return TRIGGER_TYPE.registerStatic(id, () -> TriggerType.createSerializableHook(HeavenDestinyMoment.asResource(id), hookClass, codec));
    }
}