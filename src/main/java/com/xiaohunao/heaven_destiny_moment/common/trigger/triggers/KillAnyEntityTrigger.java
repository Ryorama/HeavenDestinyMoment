package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

public abstract class KillAnyEntityTrigger implements ITrigger {
    public boolean canTrigger() {
        return true;
    }

    public static class Common extends KillAnyEntityTrigger {
        public static final KillAnyEntityTrigger.Common INSTANCE = new KillAnyEntityTrigger.Common();
        public static final MapCodec<KillAnyEntityTrigger.Common> CODEC = MapCodec.unit(INSTANCE);
    }

    public static class Moment extends KillAnyEntityTrigger {
        public static final KillAnyEntityTrigger.Moment INSTANCE = new KillAnyEntityTrigger.Moment();
        public static final MapCodec<KillAnyEntityTrigger.Moment> CODEC = MapCodec.unit(INSTANCE);
    }
}

