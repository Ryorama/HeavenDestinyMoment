package com.xiaohunao.heaven_destiny_moment.client.gui.bar.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import java.util.Locale;

public class DefaultBarRenderType implements IBarRenderType {
    public static final Codec<BossEvent.BossBarOverlay> BOSS_BAR_OVERLAY_CODEC = Codec.STRING.xmap(
            overlay -> BossEvent.BossBarOverlay.valueOf(overlay.toUpperCase(Locale.ROOT)),
            overlay -> overlay.name().toLowerCase(Locale.ROOT)
    );

    public static final Codec<BossEvent.BossBarColor> BOSS_COLOR_CODEC = Codec.STRING.xmap(
            color -> BossEvent.BossBarColor.valueOf(color.toUpperCase(Locale.ROOT)),
            color -> color.name().toLowerCase(Locale.ROOT)
    );

    public static final MapCodec<DefaultBarRenderType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BOSS_BAR_OVERLAY_CODEC.optionalFieldOf("overlay", BossEvent.BossBarOverlay.PROGRESS)
                    .forGetter(DefaultBarRenderType::getOverlay),
            BOSS_COLOR_CODEC.optionalFieldOf("color", BossEvent.BossBarColor.RED)
                    .forGetter(DefaultBarRenderType::getColor)
    ).apply(instance, DefaultBarRenderType::new));

    private final BossEvent.BossBarOverlay overlay;
    private final BossEvent.BossBarColor color;

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int SPACING = 10;
    private static final int TEXT_OFFSET = 9;

    private int maxBars = -1;

    public DefaultBarRenderType(BossEvent.BossBarOverlay overlay, BossEvent.BossBarColor color) {
        this.overlay = overlay;
        this.color = color;
    }

    public DefaultBarRenderType() {
        this(BossEvent.BossBarOverlay.PROGRESS, BossEvent.BossBarColor.RED);
    }

    @Override
    public MapCodec<? extends IBarRenderType> codec() {
        return HDMContextRegister.DEFAULT_BAR_RENDER_TYPE.get();
    }

    @Override
    public void renderBar(GuiGraphics guiGraphics, MomentBar bar, MomentInstance momentInstance, int index) {
        if (maxBars == -1) {
            maxBars = calculateMaxBars();
        }

        if (index >= maxBars) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int screenWidth = window.getGuiScaledWidth();

        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = 12 + (index * (BAR_HEIGHT + SPACING + TEXT_OFFSET));

        if (minecraft.level != null){
            ResourceLocation momentResource = momentInstance.getMomentResource();
            if (momentResource != null) {
                Component name = Component.translatable(HeavenDestinyMoment.asDescriptionId("bar." + momentResource.toLanguageKey()));
                int textWidth = minecraft.font.width(name);
                int textX = (screenWidth - textWidth) / 2;
                int textY = y - TEXT_OFFSET;
                guiGraphics.drawString(minecraft.font, name, textX, textY, 16777215, true);
            }
        }

        drawBar(guiGraphics, x, y, bar, color);
    }

    private int calculateMaxBars() {
        Window window = Minecraft.getInstance().getWindow();
        int screenHeight = window.getGuiScaledHeight();

        int barTotalHeight = BAR_HEIGHT + SPACING + TEXT_OFFSET;


        int maxHeight = screenHeight / 4;

        return Math.max(1, maxHeight / barTotalHeight);
    }


    private void drawBar(GuiGraphics guiGraphics, int x, int y, MomentBar bar, BossEvent.BossBarColor color) {
        drawBar(guiGraphics, x, y, bar, BAR_WIDTH, color,
                BossHealthOverlay.BAR_BACKGROUND_SPRITES,
                BossHealthOverlay.OVERLAY_BACKGROUND_SPRITES);

        int progress = Mth.lerpDiscrete(bar.getProgress(), 0, BAR_WIDTH);
        if (progress > 0) {
            drawBar(guiGraphics, x, y, bar, progress, color,
                    BossHealthOverlay.BAR_PROGRESS_SPRITES,
                    BossHealthOverlay.OVERLAY_PROGRESS_SPRITES);
        }
    }

    private void drawBar(GuiGraphics guiGraphics, int x, int y, MomentBar bar, int progress,
                         BossEvent.BossBarColor color, ResourceLocation[] barSprites,
                         ResourceLocation[] overlaySprites) {
        RenderSystem.enableBlend();

        guiGraphics.blitSprite(barSprites[color.ordinal()], BAR_WIDTH, BAR_HEIGHT,
                0, 0, x, y, progress, BAR_HEIGHT);

        if (this.overlay != BossEvent.BossBarOverlay.PROGRESS) {
            guiGraphics.blitSprite(overlaySprites[overlay.ordinal() - 1], BAR_WIDTH, BAR_HEIGHT,
                    0, 0, x, y, progress, BAR_HEIGHT);
        }

        RenderSystem.disableBlend();
    }

    public BossEvent.BossBarOverlay getOverlay() {
        return this.overlay;
    }

    public BossEvent.BossBarColor getColor() {
        return color;
    }

}
