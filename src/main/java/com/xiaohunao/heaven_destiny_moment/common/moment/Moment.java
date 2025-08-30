package com.xiaohunao.heaven_destiny_moment.common.moment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.DefaultInstance;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class Moment implements IMoment {
    public Optional<IBarRenderType> barRenderType = Optional.empty();
    public Optional<MomentData> momentData = Optional.empty();
    public Optional<TipSettings> tipSettings = Optional.empty();
    public Optional<ClientSettings> clientSettings = Optional.empty();
    public Optional<List<ITracker>> trackers = Optional.empty();

    public Moment() {}

    public Moment(Optional<IBarRenderType> renderType, Optional<MomentData> momentData, Optional<TipSettings> tipSettings, Optional<ClientSettings> clientSettings, Optional<List<ITracker>> trackers) {
        this.barRenderType = renderType;
        this.momentData = momentData;
        this.tipSettings = tipSettings;
        this.clientSettings = clientSettings;
        this.trackers = trackers;
    }

    @Override
    public Optional<IBarRenderType> barRenderType() {
        return barRenderType;
    }

    @Override
    public Optional<MomentData> momentData() {
        return momentData;
    }

    @Override
    public Optional<ClientSettings> clientSettings() {
        return clientSettings;
    }

    @Override
    public Optional<TipSettings> tipSettings() {
        return tipSettings;
    }



    @Override
    public Optional<List<ITracker>> trackers() {
        return trackers;
    }

    public Moment setBarRenderType(IBarRenderType barRenderType) {
        this.barRenderType = Optional.ofNullable(barRenderType);
        return this;
    }

    @Override
    public Moment setMomentData(Function<MomentData.Builder, MomentData.Builder> momentData) {
        MomentData.Builder builder = new MomentData.Builder();
        if (this.momentData.isPresent()) {
            builder = builder.converter(this.momentData.get());
        }
        this.momentData = Optional.ofNullable(momentData.apply(builder).build());
        return this;
    }

    @Override
    public Moment setClientSettings(Function<ClientSettings.Builder, ClientSettings.Builder> clientSettings) {
        ClientSettings.Builder builder = new ClientSettings.Builder();
        if (this.clientSettings.isPresent()) {
            builder = builder.converter(this.clientSettings.get());
        }
        this.clientSettings = Optional.ofNullable(clientSettings.apply(builder).build());
        return this;
    }

    @Override
    public Moment setTipSettings(Function<TipSettings.Builder, TipSettings.Builder> tipSettings) {
        TipSettings.Builder builder = new TipSettings.Builder();
        if (this.tipSettings.isPresent()) {
            builder = builder.converter(this.tipSettings.get());
        }
        this.tipSettings = Optional.ofNullable(tipSettings.apply(builder).build());
        return this;
    }

    @Override
    public Moment setTrackers(Consumer<List<ITracker>> trackers) {
        List<ITracker> trackers1 = Lists.newArrayList();
        trackers.accept(trackers1);
        this.trackers = Optional.of(trackers1);
        return this;
    }

    @Override
    public boolean isClientMomentInstanceOccupied() {
        return clientSettings.map(ClientSettings::isPresent).orElse(false);
    }

    public static <M extends IMoment> MapCodec<M> simpleCodec(Function5<Optional<IBarRenderType>, Optional<MomentData>, Optional<TipSettings>, Optional<ClientSettings>, Optional<List<ITracker>>, M> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                HDMRegistries.BAR_RENDER_TYPE.byNameCodec().optionalFieldOf("bar_render_type").forGetter(IMoment::barRenderType),
                MomentData.CODEC.optionalFieldOf("moment_data_context").forGetter(IMoment::momentData),
                TipSettings.CODEC.optionalFieldOf("tips").forGetter(IMoment::tipSettings),
                ClientSettings.CODEC.optionalFieldOf("clientSettings").forGetter(IMoment::clientSettings),
                Codec.list(ITracker.CODEC).optionalFieldOf("trackers").forGetter(IMoment::trackers)
        ).apply(instance, factory));
    }
}
