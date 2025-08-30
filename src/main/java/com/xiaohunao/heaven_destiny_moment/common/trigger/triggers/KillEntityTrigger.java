package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record KillEntityTrigger(Optional<EntityType<?>> entityType) implements ITrigger {
    public static final MapCodec<KillEntityTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entityType").forGetter(KillEntityTrigger::entityType)
    ).apply(instance, KillEntityTrigger::new));

    public static KillEntityTrigger of(EntityType<?> entityType) {
        return new KillEntityTrigger(Optional.of(entityType));
    }

    public static KillEntityTrigger any() {
        return new KillEntityTrigger(Optional.empty());
    }


    @Override
    public boolean canTrigger(AutomationContext context) {
        if (entityType.isEmpty()){
            return true;
        }
        if (context.getEntityType().isEmpty()) {
            return false;
        }
        return context.getEntityType().get().equals(entityType.get());
    }

    @Override
    public MapCodec<? extends ITrigger> codec() {
        return CODEC;
    }




}

