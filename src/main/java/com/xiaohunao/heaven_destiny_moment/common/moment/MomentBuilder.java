package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.DefaultMoment;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class MomentBuilder<T extends IMoment> {
    protected IBarRenderType barRenderType;
    //    public Area area;
    protected MomentData momentData;
    protected TipSettings tipSettings;
    protected ClientSettings clientSettings;
    protected List<ITracker> trackers;

    public abstract T build();

    public MomentBuilder<T> barRenderType(IBarRenderType barRenderType) {
        this.barRenderType = barRenderType;
        return this;
    }

//    public Moment setArea(Area area) {
//        this.area = area;
//        return this;
//    }

    public MomentBuilder<T> momentData(Function<MomentData.Builder, MomentData.Builder> momentDataFunction) {
        MomentData.Builder builder = new MomentData.Builder();
        if (this.momentData != null) {
            builder = builder.converter(this.momentData);
        }
        this.momentData = momentDataFunction.apply(builder).build();
        return this;
    }

    public MomentBuilder<T> clientSettings(Function<ClientSettings.Builder, ClientSettings.Builder> clientSettingsFunction) {
        ClientSettings.Builder builder = new ClientSettings.Builder();
        if (this.clientSettings != null) {
            builder = builder.converter(this.clientSettings);
        }
        this.clientSettings = clientSettingsFunction.apply(builder).build();
        return this;
    }

    public MomentBuilder<T> tipSettings(Function<TipSettings.Builder, TipSettings.Builder> tipSettingsFunction) {
        TipSettings.Builder builder = new TipSettings.Builder();
        if (this.tipSettings != null) {
            builder = builder.converter(this.tipSettings);
        }
        this.tipSettings = tipSettingsFunction.apply(builder).build();
        return this;
    }

    public MomentBuilder<T> trackers(Consumer<List<ITracker>> trackersConsumer) {
        List<ITracker> trackersList = Lists.newArrayList();
        trackersConsumer.accept(trackersList);
        this.trackers = trackersList;
        return this;
    }
}
