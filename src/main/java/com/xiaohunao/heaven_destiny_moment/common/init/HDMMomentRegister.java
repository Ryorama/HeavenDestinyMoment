package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.DefaultMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HDMMomentRegister {
    public static final DeferredRegister<MapCodec<? extends Moment>> MOMENT_CODEC = DeferredRegister.create(HDMRegistries.Keys.MOMENT_CODEC, HeavenDestinyMoment.MODID);

    public static final DeferredHolder<MapCodec<? extends Moment>, MapCodec<Moment>> DEFAULT_MOMENT = MOMENT_CODEC.register("default", () -> DefaultMoment.CODEC);
    public static final DeferredHolder<MapCodec<? extends Moment>, MapCodec<RaidMoment>> RAID_MOMENT = MOMENT_CODEC.register("raid", () -> RaidMoment.CODEC);

    public static void register(IEventBus modEventBus){
        MOMENT_CODEC.register(modEventBus);
    }
}
