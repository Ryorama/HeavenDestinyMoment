package com.xiaohunao.heaven_destiny_moment.compat.phase_journey;


import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.event.PhaseTriggerTriggerSubscriber;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init.PJConditions;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init.PJTriggers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class PhaseJourneyInit {

    public static void register(IEventBus modEventBus){
        PJConditions.CONDITION_CODEC.register(modEventBus);
        PJTriggers.TRIGGER_CODEC.register(modEventBus);
        NeoForge.EVENT_BUS.register(new PhaseTriggerTriggerSubscriber());
    }
}
