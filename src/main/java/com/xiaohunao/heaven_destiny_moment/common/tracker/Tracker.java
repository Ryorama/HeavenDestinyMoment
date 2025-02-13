package com.xiaohunao.heaven_destiny_moment.common.tracker;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;
import java.util.function.Function;

public class Tracker implements ITracker {
    public static final MapCodec<Tracker> CODEC = createCodec(tag -> new Tracker());
    private final Multimap<Class<? extends Event>, Consumer<? extends Event>> trackerEventMap = HashMultimap.create();

    public Tracker() {
        init();
    }

    public void init() {

    }

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

    public <E> void addEvent(Class<E> eventClass, Consumer<E> consumer) {
        @SuppressWarnings("unchecked")
        Class<Event> castedEventClass = (Class<Event>) eventClass;
        @SuppressWarnings("unchecked")
        Consumer<Event> castedConsumer = (Consumer<Event>) consumer;
        trackerEventMap.put(castedEventClass, castedConsumer);
    }

    @Override
    public void register() {
        trackerEventMap.forEach((eventClass, consumer) -> {
            @SuppressWarnings("unchecked")
            Class<Event> castedEventClass = (Class<Event>) eventClass;
            @SuppressWarnings("unchecked")
            Consumer<Event> castedConsumer = (Consumer<Event>) consumer;
            NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, castedEventClass, castedConsumer);
        });
    }

    @Override
    public void unregister() {
        trackerEventMap.forEach((eventClass, consumer) -> {
            @SuppressWarnings("unchecked")
            Consumer<Event> castedConsumer = (Consumer<Event>) consumer;
            NeoForge.EVENT_BUS.unregister(castedConsumer);
        });
    }

    @Override
    public MapCodec<? extends ITracker> codec(){
        return HDMContextRegister.DEFAULT_TRACKER.get();
    }



    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag tag) {

    }
}
