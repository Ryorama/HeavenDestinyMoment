package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientMoonSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import com.xiaohunao.heaven_destiny_moment.common.utils.ColorUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Nullable private ClientLevel level;

    @ModifyConstant(method = "renderSky", constant = @Constant(floatValue = 20.0F))
    private float renderSky(float originalSize) {
        MomentManager momentManager = MomentManager.of(level);

        Float moonSize = momentManager.getClientMomentInstance()
                .flatMap(MomentInstance::moment)
                .flatMap(Moment::clientSettings)
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonSize)
                .orElse(null);

        if (moonSize != null) {
            return moonSize;
        }
        return originalSize;
    }
    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1))
    private void renderSky(int moonTextureId, ResourceLocation originaResourceLocation) {
        MomentManager momentManager = MomentManager.of(level);

        ResourceLocation moonTexture = momentManager.getClientMomentInstance()
                .flatMap(MomentInstance::moment)
                .flatMap(Moment::clientSettings)
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonTexture)
                .orElse(originaResourceLocation);

        RenderSystem.setShaderTexture(moonTextureId, moonTexture);
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMoonPhase()I"))
    private void renderSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        MomentManager momentManager = MomentManager.of(level);
        Integer moonColor = momentManager.getClientMomentInstance()
                .flatMap(MomentInstance::moment)
                .flatMap(Moment::clientSettings)
                .flatMap(ClientSettings::clientMoonSettings)
                .flatMap(ClientMoonSettings::moonColor)
                .orElse(null);

        if (moonColor != null){
            Vector3f color = ColorUtils.colorToVector3f(moonColor);
            RenderSystem.setShaderColor(color.x,color.y,color.z, 1.0F - level.getRainLevel(partialTick));
        }
    }
}