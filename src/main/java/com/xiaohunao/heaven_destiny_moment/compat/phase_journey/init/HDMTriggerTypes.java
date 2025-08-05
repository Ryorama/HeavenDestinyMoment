package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.trigger.PhaseTrigger;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import org.confluence.phase_journey.PhaseJourney;

public class HDMTriggerTypes {
    public static final FlexibleRegister<TriggerType<?>> TRIGGER_TYPE = FlexibleRegister.create(HDMRegistries.TRIGGER_TYPE, PhaseJourney.MODID);

    public static final FlexibleHolder<TriggerType<?>,TriggerType<PhaseTrigger.Add>> ADD = TRIGGER_TYPE.registerStatic("add", () -> new TriggerType<>(PhaseTrigger.Add.class, PhaseTrigger.Add.CODEC));
    public static final FlexibleHolder<TriggerType<?>,TriggerType<PhaseTrigger.Remove>> REMOVE = TRIGGER_TYPE.registerStatic("remove", () -> new TriggerType<>( PhaseTrigger.Remove.class, PhaseTrigger.Remove.CODEC));

}