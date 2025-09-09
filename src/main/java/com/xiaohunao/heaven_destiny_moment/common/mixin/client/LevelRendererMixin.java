package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientMoonSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.xhn_lib.common.util.ColorUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

        Float moonSize = momentInstanceManager.getClientMomentInstance()
                .map(MomentInstance::getMoment)
                .flatMap(IMoment::clientSettings)
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonSize)
                .orElse(null);

        if (moonSize != null) {
            return moonSize;
        }
        return originalSize;
    }

    @WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private void renderSky(int shaderTexture, ResourceLocation textureId, Operation<Void> original) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

        if (momentInstanceManager.clientOnlyMomentInstance != null) {
            textureId = momentInstanceManager.clientOnlyMomentInstance.getMoment().clientSettings()
                    .flatMap(ClientSettings::clientMoonSettings)
                    .flatMap(ClientMoonSettings::moonTexture)
                    .orElse(textureId);
        }

        original.call(shaderTexture, textureId);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

        Integer moonColor = momentInstanceManager.getClientMomentInstance()
                .map(MomentInstance::getMoment)
                .flatMap(IMoment::clientSettings)
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonColor)
                .orElse(null);


        if (moonColor != null) {
            Vector3f color = ColorUtils.colorToVector3f(moonColor);
            RenderSystem.setShaderColor(color.x, color.y, color.z, 1.0F - level.getRainLevel(partialTick));
        }
    }
}
