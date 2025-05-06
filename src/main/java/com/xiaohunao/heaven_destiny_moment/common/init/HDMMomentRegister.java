package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.DefaultMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.DefaultInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.RaidInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public class HDMMomentRegister {
    public static final DeferredRegister<MapCodec<? extends Moment>> MOMENT_CODEC = DeferredRegister.create(HDMRegistries.Keys.MOMENT_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MomentType<?>> MOMENT_TYPE = DeferredRegister.create(HDMRegistries.MOMENT_TYPE,HeavenDestinyMoment.MODID);


    public static final DeferredHolder<MapCodec<? extends Moment>, MapCodec<Moment>> DEFAULT_MOMENT = MOMENT_CODEC.register("default", () -> DefaultMoment.CODEC);
    public static final DeferredHolder<MapCodec<? extends Moment>, MapCodec<RaidMoment>> RAID_MOMENT = MOMENT_CODEC.register("raid", () -> RaidMoment.CODEC);

    public static final DeferredHolder<MomentType<?>, MomentType<DefaultInstance>> DEFAULT = MOMENT_TYPE.register("default",
            () -> new MomentType.Builder<>(DefaultInstance::new).build());

    public static final DeferredHolder<MomentType<?>, MomentType<RaidInstance>> RAID = MOMENT_TYPE.register("raid",
            () -> new MomentType.Builder<>(RaidInstance::new).build());


    public static void register(IEventBus modEventBus){
        MOMENT_CODEC.register(modEventBus);
        MOMENT_TYPE.register(modEventBus);
    }
}
