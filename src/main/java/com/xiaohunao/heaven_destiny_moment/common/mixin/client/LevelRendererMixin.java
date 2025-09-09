package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientMoonSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Nullable
    private ClientLevel level;

    @ModifyExpressionValue(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 cacheInstance(Vec3 original, @Share("momentInstance") LocalRef<@Nullable MomentInstance> ref) {
        ref.set(MomentInstanceManager.of(level).getClientMomentInstance());
        return original;
    }

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float renderSky(float originalSize, @Share("momentInstance") LocalRef<@Nullable MomentInstance> ref) {
        MomentInstance momentInstance = ref.get();

        if (momentInstance != null) originalSize = momentInstance.getMoment().clientSettings()
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonSize)
                .orElse(originalSize);

        return originalSize;
    }

    @WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private void renderSky(int shaderTexture, ResourceLocation textureId, Operation<Void> original, @Share("momentInstance") LocalRef<@Nullable MomentInstance> ref) {
        MomentInstance instance = ref.get();

        if (instance != null) textureId = instance.getMoment().clientSettings()
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonTexture)
                .orElse(textureId);

        original.call(shaderTexture, textureId);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci, @Share("momentInstance") LocalRef<@Nullable MomentInstance> ref) {
        MomentInstance instance = ref.get();

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
