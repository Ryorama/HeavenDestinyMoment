package com.xiaohunao.heaven_destiny_moment.client.gui.hud;

import com.google.common.collect.Maps;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public class MomentBarOverlay implements LayeredDraw.Layer {
    public static final Map<UUID, MomentBar> barMap = Maps.newLinkedHashMap();

    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/demo_background.png");
    private static final ResourceLocation GUI_BARS_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/bars.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;
        int i = 0;
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(clientLevel);
        for (MomentBar bar : barMap.values()) {
            MomentInstance momentInstance = momentInstanceManager.getMomentInstance(bar.getID());
            if (momentInstance != null){
                bar.getType().renderBar(guiGraphics, bar, momentInstance, i);
                i++;
            }
        }
    }
}
