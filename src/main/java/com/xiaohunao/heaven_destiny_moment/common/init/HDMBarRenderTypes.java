package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.DefaultBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.TerrariaBarRenderType;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HDMBarRenderTypes {
    public static final FlexibleRegister<IBarRenderType> BAR_RENDER_TYPE = FlexibleRegister.create(HDMRegistries.BAR_RENDER_TYPE, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<IBarRenderType, ?> DEFAULT_BAR_RENDER_TYPE = BAR_RENDER_TYPE.registerStatic("default", DefaultBarRenderType::new);
    public static final FlexibleHolder<IBarRenderType, ?> TERRA_BAR_RENDER_TYPE = BAR_RENDER_TYPE.registerStatic("terra", TerrariaBarRenderType::new);

}
