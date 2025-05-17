package com.xiaohunao.heaven_destiny_moment.client.gui.bar.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Function;

public interface IBarRenderType {
    void renderBar(GuiGraphics guiGraphics, MomentBar bar, MomentInstance momentInstance, int index);
}
