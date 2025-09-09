package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientMoonSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Nullable
    private ClientLevel level;

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float renderSky(float originalSize) {
        MomentInstance instance = MomentInstanceManager.of(level).clientOnlyMomentInstance;

        if (instance != null) originalSize = instance.getMoment().clientSettings()
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonSize)
                .orElse(originalSize);

        return originalSize;
    }

    @WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private void renderSky(int shaderTexture, ResourceLocation textureId, Operation<Void> original) {
        MomentInstance instance = MomentInstanceManager.of(level).clientOnlyMomentInstance;

        if (instance != null) textureId = instance.getMoment().clientSettings()
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonTexture)
                .orElse(textureId);

        original.call(shaderTexture, textureId);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        MomentInstance instance = MomentInstanceManager.of(level).clientOnlyMomentInstance;

        if (instance != null) instance.getMoment().clientSettings()
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonColor)
                .ifPresent(color -> {
                    float r = (color >> 16 & 255) / 255.0F;
                    float g = (color >> 8 & 255) / 255.0F;
                    float b = (color & 255) / 255.0F;
                    RenderSystem.setShaderColor(r, g, b, 1.0F - level.getRainLevel(partialTick));
                });
    }
}
