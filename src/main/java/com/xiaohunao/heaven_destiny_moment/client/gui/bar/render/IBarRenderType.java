package com.xiaohunao.heaven_destiny_moment.client.gui.bar.render;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Function;

public interface IBarRenderType {
    Codec<IBarRenderType> CODEC = Codec.lazyInitialized(() -> HDMRegistries.Suppliers.BAR_RENDER_TYPE_CODEC.get().byNameCodec()).dispatch(IBarRenderType::codec, Function.identity());

    MapCodec<? extends IBarRenderType> codec();

    void renderBar(GuiGraphics guiGraphics, MomentBar bar, MomentInstance momentInstance, int index);
}
