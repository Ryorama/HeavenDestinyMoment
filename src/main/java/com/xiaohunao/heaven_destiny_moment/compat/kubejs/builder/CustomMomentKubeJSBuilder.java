package com.xiaohunao.heaven_destiny_moment.compat.kubejs.builder;

import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.compat.kubejs.KubeJSMoment;
import net.minecraft.resources.ResourceLocation;

public class CustomMomentKubeJSBuilder extends MomentKubeJSBuilder{
    public CustomMomentKubeJSBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public Moment<?> createObject() {
        return new KubeJSMoment(barRenderType, area, momentData, tipSettings, clientSettings,trackers);
    }
}
