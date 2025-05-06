package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.resources.ResourceLocation;

public class TriggerType<T extends ITrigger> {
    public static final Codec<TriggerType<?>> CODEC = ResourceLocation.CODEC.xmap(
            location -> HDMRegistries.Suppliers.TRIGGER_TYPE.get().get(location),
            hookType -> HDMRegistries.Suppliers.TRIGGER_TYPE.get().getKey(hookType)
    );
    private final ResourceLocation id;
    private final Class<T> triggerClass;
    private final boolean isSerializable;

    private TriggerType(ResourceLocation id, Class<T> triggerClass) {
        this.id = id;
        this.triggerClass = triggerClass;
        this.isSerializable = ISerializableTrigger.class.isAssignableFrom(triggerClass);
    }

    @SuppressWarnings("unchecked")
    private TriggerType(ResourceLocation id, Class<T> triggerClass, MapCodec<T> codec) {
        this(id, triggerClass);
        if (!ISerializableTrigger.class.isAssignableFrom(triggerClass)) {
            throw new IllegalArgumentException("Hook class " + triggerClass.getName() + " must implement ISerializableHook");
        }
        ISerializableTrigger.register(id, (Class<? extends ISerializableTrigger>) triggerClass, (MapCodec<? extends ISerializableTrigger>) codec);
    }

    public ResourceLocation getId() {
        return id;
    }

    public Class<T> getTriggerClass() {

        return triggerClass;
    }

    public boolean isSerializable() {
        return isSerializable;
    }

    @SuppressWarnings("unchecked")
    public MapCodec<? extends ISerializableTrigger> getCodec() {
        if (!isSerializable) {
            throw new UnsupportedOperationException("Hook type " + id + " is not serializable");
        }
        return ISerializableTrigger.CLASS_TO_CODEC.get((Class<? extends ISerializableTrigger>) triggerClass);
    }

    public static <T extends ITrigger> TriggerType<T> createHook(ResourceLocation id, Class<T> hookClass) {
        return new TriggerType<>(id, hookClass);
    }

    public static <T extends ISerializableTrigger> TriggerType<T> createSerializableHook(ResourceLocation id, Class<T> hookClass, MapCodec<T> codec) {
        return new TriggerType<>(id, hookClass, codec);
    }
}