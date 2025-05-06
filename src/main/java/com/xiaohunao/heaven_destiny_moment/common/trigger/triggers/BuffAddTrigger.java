package com.xiaohunao.heaven_destiny_moment.common.trigger.triggers;

import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface BuffAddTrigger extends ITrigger {

    boolean onBuffAdd(LivingEntity entity, MobEffect effect, MobEffectInstance effectInstance);
} 