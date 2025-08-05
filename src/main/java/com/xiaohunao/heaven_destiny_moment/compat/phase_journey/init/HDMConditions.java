package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.condition.PhaseJourneyCondition;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import org.confluence.phase_journey.PhaseJourney;

public class HDMConditions {
    public static final FlexibleRegister<MapCodec<? extends ICondition>> CONDITION_CODEC = FlexibleRegister.create(HDMRegistries.CONDITION_CODEC, PhaseJourney.MODID);

    public static final FlexibleHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> PHASE_JOURNEY = CONDITION_CODEC.registerStatic("phase", () -> PhaseJourneyCondition.CODEC);
}
