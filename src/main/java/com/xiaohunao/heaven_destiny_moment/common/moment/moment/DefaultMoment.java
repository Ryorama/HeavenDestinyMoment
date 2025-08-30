package com.xiaohunao.heaven_destiny_moment.common.moment.moment;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMomentRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.DefaultInstance;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class DefaultMoment extends Moment {
    public static final MapCodec<DefaultMoment> CODEC = simpleCodec(DefaultMoment::new);

    public DefaultMoment() {
        super();
    }

    public DefaultMoment(Optional<IBarRenderType> renderType, Optional<Area> area, Optional<MomentData> momentDataContext, Optional<TipSettings> tipSettingsContext, Optional<ClientSettings> clientSettings, Optional<List<ITracker>> trackers) {
        super(renderType, area, momentDataContext, tipSettingsContext, clientSettings, trackers);
    }

    @Override
    public MomentInstance newMomentInstance(Level level, Moment momentResourceKey) {
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
