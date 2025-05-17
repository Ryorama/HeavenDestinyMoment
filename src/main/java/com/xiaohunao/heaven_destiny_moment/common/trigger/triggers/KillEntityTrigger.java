package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ISerializableTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public abstract class KillEntityTrigger implements ISerializableTrigger {
    private final EntityType<?> entityType;

    public KillEntityTrigger(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public boolean canTrigger(EntityType<?> entityType) {
        return entityType == this.entityType;
    }

    public static class Common extends KillEntityTrigger {
        public static final MapCodec<Common> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(KillEntityTrigger::getEntityType)
        ).apply(instance, Common::new));

        public Common(EntityType<?> entityType) {
            super(entityType);
        }

        public static Common of(EntityType<?> entityType) {
            return new Common(entityType);
        }
    }

    public static class Moment extends KillEntityTrigger {
        public static final MapCodec<Moment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(KillEntityTrigger::getEntityType)
        ).apply(instance, Moment::new));

        public Moment(EntityType<?> entityType) {
            super(entityType);
        }

        public static Moment of(EntityType<?> entityType) {
            return new Moment(entityType);
        }
    }
}
