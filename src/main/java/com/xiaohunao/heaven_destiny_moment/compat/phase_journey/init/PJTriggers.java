package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.trigger.PhaseTrigger;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import org.confluence.phase_journey.PhaseJourney;

public class PJTriggers {
    public static final FlexibleRegister<MapCodec<? extends ITrigger>> TRIGGER_CODEC = FlexibleRegister.create(HDMRegistries.Keys.TRIGGER_CODEC, PhaseJourney.MODID);

    public static final FlexibleHolder<MapCodec<? extends ITrigger>, MapCodec<? extends ITrigger>> ADD_PHASE = TRIGGER_CODEC.registerStatic("add_phase", () -> PhaseTrigger.Add.CODEC);
    public static final FlexibleHolder<MapCodec<? extends ITrigger>, MapCodec<? extends ITrigger>> REMOVE_PHASE = TRIGGER_CODEC.registerStatic("remove_phase", () -> PhaseTrigger.Remove.CODEC);

}