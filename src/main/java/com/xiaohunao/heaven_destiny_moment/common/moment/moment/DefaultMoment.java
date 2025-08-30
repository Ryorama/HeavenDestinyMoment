package com.xiaohunao.heaven_destiny_moment.common.moment.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.DefaultInstance;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DefaultMoment extends Moment {
    public static final MapCodec<DefaultMoment> CODEC = simpleCodec(DefaultMoment::new);

    public DefaultMoment(Optional<IBarRenderType> iBarRenderType, Optional<MomentData> momentData, Optional<TipSettings> tipSettings, Optional<ClientSettings> clientSettings, Optional<List<ITracker>> iTrackers) {
        super(iBarRenderType, momentData, tipSettings, clientSettings, iTrackers);
    }

    @Override
    public MomentInstance newMomentInstance(Level level, IMoment momentResourceKey) {
        return new DefaultInstance(level, momentResourceKey);
    }

    @Override
    public MapCodec<? extends DefaultMoment> codec() {
        return CODEC;
    }



    public static class Builder extends MomentBuilder<DefaultMoment> {
        @Override
        public DefaultMoment build() {
            return new DefaultMoment(
                    Optional.ofNullable(barRenderType),
                    Optional.ofNullable(momentData),
                    Optional.ofNullable(tipSettings),
                    Optional.ofNullable(clientSettings),
                    Optional.ofNullable(trackers)
            );
        }
    }
}
