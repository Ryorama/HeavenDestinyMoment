package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.DefaultBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.SlimeRainBarRenderType;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;

public class HDMBarRenderTypes {
    public static final FlexibleRegister<IBarRenderType> BAR_RENDER_TYPE = FlexibleRegister.create(HDMRegistries.BAR_RENDER_TYPE, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<IBarRenderType, ?> DEFAULT_BAR_RENDER_TYPE = BAR_RENDER_TYPE.registerStatic("default", DefaultBarRenderType::new);
    public static final FlexibleHolder<IBarRenderType, ?> SLIME_BAR_RENDER_TYPE = BAR_RENDER_TYPE.registerStatic("slime_rain", SlimeRainBarRenderType::new);

}
