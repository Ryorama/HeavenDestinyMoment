package com.xiaohunao.heaven_destiny_moment.compat.kubejs.builder;

import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import net.minecraft.resources.ResourceLocation;

public class RaidMomentKubeJSBuilder extends MomentKubeJSBuilder{
    private int readyTime;

    public RaidMomentKubeJSBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public Moment<?> createObject() {
        return new RaidMoment(barRenderType, area, momentData, tipSettings, clientSettings,trackers,readyTime);
    }

    public RaidMomentKubeJSBuilder readyTime(int readyTime) {
        this.readyTime = readyTime;
        return this;
    }
}
