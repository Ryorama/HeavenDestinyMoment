package com.xiaohunao.heaven_destiny_moment.client.gui.bar.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import com.xiaohunao.heaven_destiny_moment.common.actuator.SimpleEntitySpawnActuator;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlimeRainBarRenderType implements IBarRenderType {
    // 存储每个时刻条的动画状态
    private static final Map<UUID, AnimationState> animationStates = new HashMap<>();
    
    // 动画持续时间（毫秒）
    private static final long ANIMATION_DURATION = 500;
    
    // 是否启用动画效果
    private static final boolean ENABLE_ANIMATION = true;
    
    // 用于记录动画状态的类
    private static class AnimationState {
        float currentX;
        float currentY;
        float targetX;
        float targetY;
        long startTime;
        boolean isAnimating;
        
        public AnimationState(float targetX, float targetY) {
            this.currentX = targetX + 100; // 从右侧100像素外开始
            this.currentY = targetY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.startTime = System.currentTimeMillis();
            this.isAnimating = true;
        }
        
        public void updateTarget(float targetX, float targetY) {
            if (Math.abs(this.targetX - targetX) > 1 || Math.abs(this.targetY - targetY) > 1) {
                // 如果目标位置发生明显变化，重新开始动画
                this.targetX = targetX;
                this.targetY = targetY;
                this.startTime = System.currentTimeMillis();
                this.isAnimating = true;
            }
        }
        
        public void update() {
            if (!isAnimating) return;
            
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            
            if (elapsedTime >= ANIMATION_DURATION) {
                // 动画完成
                currentX = targetX;
                currentY = targetY;
                isAnimating = false;
                return;
            }
            
            // 使用平滑的缓动函数计算当前位置
            float progress = (float) elapsedTime / ANIMATION_DURATION;
            progress = easeOutQuad(progress);
            
            currentX = lerp(currentX, targetX, progress);
            currentY = lerp(currentY, targetY, progress);
        }
        
        // 平方缓出函数，使动画看起来更自然
        private float easeOutQuad(float t) {
            return t * (2 - t);
        }
        
        // 线性插值函数
        private float lerp(float start, float end, float t) {
            return start + (end - start) * t;
        }
    }
    @Override
    public void renderBar(GuiGraphics guiGraphics, MomentBar bar, MomentInstance momentInstance, int index) {
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        int iconSize = 16;


        int width = guiWidth / 6;
        int backgroundWidth = width + iconSize;
        int backgroundHeight = iconSize * 2;

        int startX = guiWidth - backgroundWidth + 3;
        int startY = guiHeight - backgroundHeight + 10;
        animation(guiGraphics, bar,momentInstance, startX, startY, iconSize, backgroundWidth, backgroundHeight);
    }

    private static void animation(GuiGraphics guiGraphics, MomentBar bar,MomentInstance momentInstance, int startX, int startY, int iconSize, int backgroundWidth, int backgroundHeight) {
        PoseStack poseStack = guiGraphics.pose();
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation momentResource = momentInstance.getMomentResource();
        ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(momentResource.getNamespace(), "textures/gui/bars/icon/" + momentResource.getPath() + "_icon.png");
        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(momentResource.getNamespace(), "textures/gui/bars/background/" + momentResource.getPath() + "_background.png");
        Component momentName = Component.translatable(momentInstance.getMomentResource().toLanguageKey());

        int totalScore = momentInstance.getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER).getTotalScore();
        momentInstance.getTryRequiredKill().forEach((actuator, requiredKill) -> {
            if (actuator instanceof SimpleEntitySpawnActuator){
                int percent = (int) ((float) totalScore / requiredKill.totalScore() * 100);
                Component momentKillDescription = Component.translatable(momentInstance.getMomentResource().toLanguageKey() + ".bar_kill_description",percent);

                if (ENABLE_ANIMATION) {
                    // 生成唯一ID，用于识别不同的条
                    // 获取或创建动画状态
                    AnimationState state = animationStates.computeIfAbsent(bar.getID(), k -> new AnimationState(startX, startY));

                    // 更新目标位置（如果位置发生变化）
                    state.updateTarget(startX, startY);

                    // 更新当前位置
                    state.update();

                    // 使用当前位置进行渲染
                    int currentX = Math.round(state.currentX);
                    int currentY = Math.round(state.currentY);

                    poseStack.pushPose();

                    // 根据动画状态应用一些效果
                    float alpha = 1.0f;
                    if (state.isAnimating) {
                        // 计算完成百分比
                        long elapsedTime = System.currentTimeMillis() - state.startTime;
                        float progress = Math.min(1.0f, (float) elapsedTime / ANIMATION_DURATION);
                        // 渐入效果
                        alpha = progress;

                        // 可以添加额外的动画效果，如缩放或旋转
                        float scale = 0.8f + 0.2f * progress; // 从80%大小逐渐到100%
                        poseStack.scale(scale, scale, 1.0f);
                        currentX = Math.round(currentX / scale);
                        currentY = Math.round(currentY / scale);
                    }

                    // 绘制图标
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
                    guiGraphics.blit(icon, currentX, currentY - iconSize, 0, 0, iconSize, iconSize, iconSize, iconSize);

                    // 绘制背景
                    guiGraphics.blit(MomentBarOverlay.BACKGROUND, currentX - 3, currentY, 0, 0, backgroundWidth + 3, backgroundHeight, backgroundWidth + 3, backgroundHeight);

                    // 绘制文本
                    int textColor = 0xFFFFFF | (Math.round(alpha * 255) << 24);
                    guiGraphics.drawString(minecraft.font, momentName, currentX + iconSize + 8, currentY - iconSize + 4, textColor);
                    guiGraphics.drawString(minecraft.font, momentKillDescription, currentX + iconSize, currentY - iconSize + 18, textColor);

                    // 绘制进度条
                    DefaultBarRenderType.drawCustomWidthBar(guiGraphics, bar, currentX + 2, currentY + 15, backgroundWidth - 10, 5, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_6);

                    // 重置颜色
                    guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                    poseStack.popPose();
                } else {
                    poseStack.pushPose();
                    guiGraphics.blit(icon, startX, startY - iconSize, 0, 0, iconSize, iconSize, iconSize, iconSize);
                    guiGraphics.blit(background, startX - 3, startY, 0, 0, backgroundWidth + 3, backgroundHeight, backgroundWidth + 3, backgroundHeight);
                    guiGraphics.drawString(minecraft.font, momentName, startX + iconSize + 8, startY - iconSize + 4, 0xFFFFFF);
                    guiGraphics.drawString(minecraft.font, momentKillDescription, startX + iconSize, startY - iconSize + 18, 0xFFFFFF);
                    bar.updateProgress(0.5f);
                    DefaultBarRenderType.drawCustomWidthBar(guiGraphics, bar, startX + 2, startY + 15, backgroundWidth - 10, 5, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_6);
                    poseStack.popPose();
                }
            }
        });
    }
}

