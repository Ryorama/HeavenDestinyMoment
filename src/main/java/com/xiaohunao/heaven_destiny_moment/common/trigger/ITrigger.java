package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.core.Registry;

public interface ITrigger {
    Codec<ITrigger> CODEC = Codec.lazyInitialized(() -> HDMRegistries.Suppliers.TRIGGER_TYPE.get().byNameCodec()).dispatch(
            hook -> {
                Registry<TriggerType<?>> registry = HDMRegistries.Suppliers.TRIGGER_TYPE.get();
                for (TriggerType<?> type : registry) {
                    if (type.getTriggerClass().isInstance(hook)) {
                        return type;
                    }
                }
                throw new IllegalStateException("Unknown hook type for " + hook.getClass());
            },
            hookType -> {
                if (hookType.isSerializable()) {
                    return hookType.getCodec();
                } else {
                    return MapCodec.unit(() -> (ITrigger) HDMRegistries.Suppliers.TRIGGER_TYPE.get().get(
                            HDMRegistries.Suppliers.TRIGGER_TYPE.get().getKey(hookType)
                    ));
                }
            }
    );
}
