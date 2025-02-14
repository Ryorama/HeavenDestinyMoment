package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.Lists;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Moment<T extends Moment<?>> implements IMoment {
    public Optional<IBarRenderType> barRenderType = Optional.empty();
    public Optional<Area> area = Optional.empty();
    public Optional<MomentData> momentData = Optional.empty();
    public Optional<TipSettings> tipSettings = Optional.empty();
    public Optional<ClientSettings> clientSettings = Optional.empty();
    public Optional<List<ITracker>> trackers = Optional.empty();


    public Moment() {}

    public Moment(Optional<IBarRenderType> renderType, Optional<Area> area, Optional<MomentData> momentData,
                  Optional<TipSettings> tipSettings, Optional<ClientSettings> clientSettings, Optional<List<ITracker>> trackers) {
        this.barRenderType = renderType;
        this.area = area;
        this.momentData = momentData;
        this.tipSettings = tipSettings;
        this.clientSettings = clientSettings;
        this.trackers = trackers;
    }


    public abstract MomentInstance<?> newMomentInstance(Level level, ResourceKey<Moment<?>> momentResourceKey);

    public boolean isInArea(ServerLevel level, BlockPos blockPos) {
        return area.map(area1 -> area1.matches(level,blockPos)).orElse(true);
    }

    public Optional<IBarRenderType> barRenderType() {
        return barRenderType;
    }

    public Optional<MomentData> momentData() {
        return momentData;
    }

    public Optional<ClientSettings> clientSettings() {
        return clientSettings;
    }

    public Optional<TipSettings> tipSettings() {
        return tipSettings;
    }

    public Optional<Area> area() {
        return area;
    }

    public Optional<List<ITracker>> trackers() {
        return trackers;
    }

    public Moment<T> setBarRenderType(IBarRenderType barRenderType) {
        this.barRenderType = Optional.of(barRenderType);
        return this;
    }

    public Moment<T> setArea(Area area) {
        this.area = Optional.of(area);
        return this;
    }

    public Moment<T> setMomentData(Function<MomentData.Builder,MomentData.Builder> momentData) {
        this.momentData = Optional.of(momentData.apply(new MomentData.Builder()).build());
        return this;
    }


    public Moment<T> setClientSettings(Function<ClientSettings.Builder,ClientSettings.Builder> clientSettings) {
        this.clientSettings = Optional.of(clientSettings.apply(new ClientSettings.Builder()).build());
        return this;
    }

    public Moment<T> setTipSettings(Function<TipSettings.Builder,TipSettings.Builder> tipSettings) {
        this.tipSettings = Optional.of(tipSettings.apply(new TipSettings.Builder()).build());
        return this;
    }

    public Moment<?> setTrackers(Consumer<List<ITracker>> trackers) {
        List<ITracker> trackers1 = Lists.newArrayList();
        trackers.accept(trackers1);
        this.trackers = Optional.of(trackers1);
        return this;
    }

    public boolean isClientMomentInstanceOccupied(){
        return clientSettings.map(ClientSettings::isPresent).orElse(false);
    }
}
