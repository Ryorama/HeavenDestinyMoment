package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.Codec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;

public interface ITrigger {
    Codec<ITrigger> CODEC = HDMRegistries.TRIGGER_TYPE.byNameCodec().dispatch(trigger -> {
        for (TriggerType<?> type : HDMRegistries.TRIGGER_TYPE) {
            if (type.clazz().isInstance(trigger)) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown triggerType for " + trigger.getClass());
    }, TriggerType::codec);
}
