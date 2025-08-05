package com.xiaohunao.heaven_destiny_moment.compat;

import com.xiaohunao.heaven_destiny_moment.compat.champions.MomentRegister;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.PhaseJourneyInit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class LoadedCompat {
    public final static boolean KJS = ModList.get().isLoaded("kubejs");
    public final static boolean CHAMPIONS = ModList.get().isLoaded("champions");
    public final static boolean PHASE_JOURNEY = ModList.get().isLoaded("phase_journey");

    public static void register(IEventBus modEventBus){
        if (KJS){
//            com.xiaohunao.heaven_destiny_moment.compat.kubejs.MomentRegister.MOMENT_TYPE.register(modEventBus);
//            com.xiaohunao.heaven_destiny_moment.compat.kubejs.MomentRegister.MOMENT_CODEC.register(modEventBus);
        }

        if (CHAMPIONS){
            MomentRegister.ATTACHABLE_CODEC.register(modEventBus);
        }

        if (PHASE_JOURNEY){
            PhaseJourneyInit.register(modEventBus);
        }
    }
}
