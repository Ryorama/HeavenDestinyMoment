package com.xiaohunao.heaven_destiny_moment.compat.kubejs.builder;

import com.google.common.collect.Lists;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.DefaultMoment;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class MomentKubeJSBuilder extends BuilderBase<Moment<?>> {
    public Optional<IBarRenderType> barRenderType = Optional.empty();
    public Optional<Area> area = Optional.empty();
    public Optional<MomentData> momentData = Optional.empty();
    public Optional<TipSettings> tipSettings = Optional.empty();
    public Optional<ClientSettings> clientSettings = Optional.empty();
    public Optional<List<ITracker>> trackers;

    public MomentKubeJSBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public Moment<?> createObject() {
        return new DefaultMoment(barRenderType, area, momentData, tipSettings, clientSettings,trackers);
    }

    public MomentKubeJSBuilder barRenderType(IBarRenderType barRenderType) {
        this.barRenderType = Optional.of(barRenderType);
        return this;
    }

    public MomentKubeJSBuilder area(Area area) {
        this.area = Optional.of(area);
        return this;
    }

    public MomentKubeJSBuilder momentData(Function<MomentData.Builder,MomentData.Builder> momentData) {
        this.momentData = Optional.of(momentData.apply(new MomentData.Builder()).build());
        return this;
    }


    public MomentKubeJSBuilder clientSettings(Function<ClientSettings.Builder,ClientSettings.Builder> clientSettings) {
        this.clientSettings = Optional.of(clientSettings.apply(new ClientSettings.Builder()).build());
        return this;
    }

    public MomentKubeJSBuilder tipSettings(Function<TipSettings.Builder,TipSettings.Builder> tipSettings) {
        this.tipSettings = Optional.of(tipSettings.apply(new TipSettings.Builder()).build());
        return this;
    }
    public MomentKubeJSBuilder trackers(Consumer<List<ITracker>> trackers) {
        List<ITracker> trackers1 = Lists.newArrayList();
        trackers.accept(trackers1);
        this.trackers = Optional.of(trackers1);
        return this;
    }
}
