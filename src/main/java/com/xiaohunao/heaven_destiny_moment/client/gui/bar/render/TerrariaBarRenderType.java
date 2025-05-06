package com.xiaohunao.heaven_destiny_moment.client.gui.bar.render;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.gui.GuiGraphics;

public class TerrariaBarRenderType implements IBarRenderType {
    public static final MapCodec<TerrariaBarRenderType> CODEC = MapCodec.unit(new TerrariaBarRenderType());

    @Override
    public MapCodec<? extends IBarRenderType> codec() {
        return HDMContextRegister.TERRA_BAR_RENDER_TYPE.get();
    }

    @Override
    public void renderBar(GuiGraphics guiGraphics, MomentBar bar, MomentInstance momentInstance, int index) {
//        Minecraft minecraft = Minecraft.getInstance();
//        int guiWidth = guiGraphics.guiWidth();
//        int guiHeight = guiGraphics.guiHeight();
//        int iconSize = 16;
//        Component iconName = bar.getName();
//        ResourceKey<Moment> momentKey = bar.getMomentKey();
//        ResourceLocation resource = new ResourceLocation(momentKey.location().getNamespace(), "textures/gui/bars/icon/" + momentKey.location().getPath() + "_icon.png");
//
//        Map<UUID, MomentInstance> runMoment = MomentCap.getCap(minecraft.level).getRunMoment();
//        MomentInstance momentInstance = runMoment.get(bar.getId());
//
//        if (momentInstance != null) {
//            int wave = momentInstance.getWave();
//            float percent = momentInstance.remainingEnemyPercent();
//            int waveCount = momentInstance.getWaveCount();
//
////            MutableComponent componentWave = Component.translatable(HeavenDestinyMoment.asDescriptionId("gui.bar.name.wave"), wave, waveCount, (int) (percent * 100));
//
//
////            int width = minecraft.font.width(componentWave);
//            int width = guiWidth / 4;
//            int backgroundWidth = width + iconSize;
//            int backgroundHeight = iconSize * 2;
//
//            int startX = guiWidth - backgroundWidth;
//            int startY = guiHeight - backgroundHeight + (iconSize / 2);
//
//            int center = startX + (backgroundWidth) / 2;
//
//
//            guiGraphics.blit(MomentBarOverlay.BACKGROUND, startX, startY, 0, 0, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);
//            DefaultBarRenderType.drawBar(guiGraphics, bar,startX + 4 ,startY + iconSize - 4,width);
////            guiGraphics.drawString(minecraft.font, componentWave, center, startY, 0xFFFFFF);
//
//
//            int size = minecraft.font.width(iconName) + iconSize;
//            int x = startX + 4;
//            int y = startY - iconSize;
//            guiGraphics.blit(resource, x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
//            guiGraphics.drawString(minecraft.font, iconName, x + iconSize, y + 4, 0xFFFFFF);

    }
}

