package com.xiaohunao.heaven_destiny_moment.common.trigger;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface ISerializableTrigger extends ITrigger {
    BiMap<Class<? extends ISerializableTrigger>, ResourceLocation> CLASS_TO_ID = HashBiMap.create();
    BiMap<Class<? extends ISerializableTrigger>, MapCodec<? extends ISerializableTrigger>> CLASS_TO_CODEC = HashBiMap.create();

    Codec<ISerializableTrigger> CODEC = ResourceLocation.CODEC.dispatch(
            ISerializableTrigger::getRegistryId,
            id -> CLASS_TO_CODEC.get(CLASS_TO_ID.inverse().get(id))
    );

    default MapCodec<? extends ISerializableTrigger> codec() {
        return CLASS_TO_CODEC.get(this.getClass());
    }

    default ResourceLocation getRegistryId() {
        ResourceLocation id = CLASS_TO_ID.get(this.getClass());
        if (id == null) {
            throw new IllegalStateException("Hook " + this.getClass().getName() + " is not registered");
        }
        return id;
    }

    static boolean isRegistered(Class<? extends ISerializableTrigger> hookClass) {
        return CLASS_TO_ID.containsKey(hookClass);
    }

    @Nullable
    static Class<? extends ISerializableTrigger> getClassById(ResourceLocation id) {
        return CLASS_TO_ID.inverse().get(id);
    }

    static TriggerType<?> getHookType(Class<? extends ISerializableTrigger> hookClass) {
        return HDMRegistries.TRIGGER_TYPE.get(CLASS_TO_ID.get(hookClass));
    }

    static void register(ResourceLocation id, Class<? extends ISerializableTrigger> hookClass, MapCodec<? extends ISerializableTrigger> codec) {
        CLASS_TO_ID.put(hookClass, id);
        CLASS_TO_CODEC.put(hookClass, codec);
    }
} 