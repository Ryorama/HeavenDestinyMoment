package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IMoment extends ICodec<IMoment> {
    Codec<IMoment> CODEC = Codec.lazyInitialized(() -> {
        return HDMRegistries.MOMENT_CODEC.byNameCodec(); // 必须在Supplier里延迟加载，不能使用方法引用
    }).dispatch(IMoment::codec, Function.identity());

    MapCodec<? extends IMoment> codec();

    MomentInstance newMomentInstance(Level level, IMoment moment);

    Optional<IBarRenderType> barRenderType();

    Optional<MomentData> momentData();

    Optional<ClientSettings> clientSettings();

    Optional<TipSettings> tipSettings();

    Optional<List<ITracker>> trackers();

    Moment setMomentData(Function<MomentData.Builder, MomentData.Builder> momentData);

    Moment setClientSettings(Function<ClientSettings.Builder, ClientSettings.Builder> clientSettings);

    Moment setTipSettings(Function<TipSettings.Builder, TipSettings.Builder> tipSettings);

    Moment setTrackers(Consumer<List<ITracker>> trackers);

    default boolean isClientMomentInstanceOccupied() {
        return clientSettings().map(ClientSettings::isPresent).orElse(false);
    }
}
