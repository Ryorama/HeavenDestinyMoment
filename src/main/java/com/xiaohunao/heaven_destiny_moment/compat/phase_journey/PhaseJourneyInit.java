package com.xiaohunao.heaven_destiny_moment.compat.phase_journey;


import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.event.PhaseTriggerTriggerSubscriber;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init.HDMTriggerTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class PhaseJourneyInit {

    public static void register(IEventBus modEventBus){
        HDMConditions.CONDITION_CODEC.register(modEventBus);
        HDMTriggerTypes.TRIGGER_TYPE.register(modEventBus);
        NeoForge.EVENT_BUS.register(new PhaseTriggerTriggerSubscriber());
    }
}
