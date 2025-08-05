package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.xiaohunao.heaven_destiny_moment.common.event.LivingAttackEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        LivingAttackEvent post = NeoForge.EVENT_BUS.post(new LivingAttackEvent(livingEntity, source, amount));
        if (post.isCanceled()) {
            cir.cancel();
        }
    }
}
