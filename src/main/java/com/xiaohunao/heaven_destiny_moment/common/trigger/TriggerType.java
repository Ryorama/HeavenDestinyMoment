package com.xiaohunao.heaven_destiny_moment.common.trigger;

import com.mojang.serialization.MapCodec;

public record TriggerType<T extends ITrigger>(Class<T> clazz, MapCodec<T> codec) {
}
