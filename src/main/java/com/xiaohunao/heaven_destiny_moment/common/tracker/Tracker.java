package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class Tracker implements ITracker {
    public static final MapCodec<Tracker> CODEC = createCodec(tag -> new Tracker());
    private final Multimap<Class<? extends Event>, Consumer<? extends Event>> trackerEventMap = HashMultimap.create();

    protected UUID instanceUUID;


    protected static <T extends Tracker> MapCodec<T> createCodec(Function<CompoundTag, T> factory) {
        return CompoundTag.CODEC.xmap(
                compoundTag -> {
                    T tracker = factory.apply(compoundTag);
                    tracker.deserializeNBT(compoundTag);
                    return tracker;
                },
                Tracker::serializeNBT
        ).fieldOf("tracker");
    }



    @Override
    public MapCodec<? extends ITracker> codec(){
        return CODEC;
    }



    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag tag) {
    }
}
