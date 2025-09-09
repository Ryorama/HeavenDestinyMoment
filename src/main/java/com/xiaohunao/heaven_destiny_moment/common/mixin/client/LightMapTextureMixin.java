package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class LightMapTextureMixin {
    @Inject(method = "updateLightTexture", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F"))
    private void updateLightTexture(float p_109882_, CallbackInfo ci, @Local ClientLevel clientlevel, @Local Vector3f vector3f) {
        MomentInstance instance = MomentInstanceManager.of(clientlevel).clientOnlyMomentInstance;

        if (instance != null) instance.getMoment().clientSettings()
                .flatMap(ClientSettings::environmentColor)
                .ifPresent(color -> {
                    float r = (color >> 16 & 255) / 255.0F;
                    float g = (color >> 8 & 255) / 255.0F;
                    float b = (color & 255) / 255.0F;
                    vector3f.set(r, g, b);
                });
    }
}
