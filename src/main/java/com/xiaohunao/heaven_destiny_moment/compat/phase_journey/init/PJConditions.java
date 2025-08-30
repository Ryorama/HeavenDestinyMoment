package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.condition.PhaseJourneyCondition;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;
import com.xiaohunao.xhn_lib.api.register.register.MapCodecFlexibleRegister;
import org.confluence.phase_journey.PhaseJourney;

public class PJConditions {
    public static final MapCodecFlexibleRegister<ICondition> CONDITION_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.CONDITION_CODEC, PhaseJourney.MODID).
            addMapCodec(
                    "phase", PhaseJourneyCondition.CODEC
            );
}
