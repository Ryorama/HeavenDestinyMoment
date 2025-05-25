package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.BlockBreakTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.KillAnyEntityTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.LevelTickTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.RandomLevelTickTrigger;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.trigger.PhaseTrigger;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import org.confluence.phase_journey.PhaseJourney;

public class HDMTriggerTypes {
    public static final FlexibleRegister<TriggerType<?>> TRIGGER_TYPE = FlexibleRegister.create(HDMRegistries.TRIGGER_TYPE, PhaseJourney.MODID, TriggerTypeManager.getInstance());

    public static final FlexibleHolder<TriggerType<?>,TriggerType<PhaseTrigger.Add>> ADD = registerSerializable("add", PhaseTrigger.Add.class, PhaseTrigger.Add.CODEC);
    public static final FlexibleHolder<TriggerType<?>,TriggerType<PhaseTrigger.Remove>> REMOVE = registerSerializable("remove", PhaseTrigger.Remove.class, PhaseTrigger.Remove.CODEC);


    private static <T extends ITrigger> FlexibleHolder<TriggerType<?>, TriggerType<T>> register(String id, Class<T> hookClass) {
        return TRIGGER_TYPE.registerStatic(id, () -> TriggerType.createHook(HeavenDestinyMoment.asResource(id), hookClass));
    }

    private static <T extends ISerializableTrigger> FlexibleHolder<TriggerType<?>, TriggerType<T>> registerSerializable(String id, Class<T> hookClass, MapCodec<T> codec) {
        return TRIGGER_TYPE.registerStatic(id, () -> TriggerType.createSerializableHook(HeavenDestinyMoment.asResource(id), hookClass, codec));
    }
}