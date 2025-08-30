package com.xiaohunao.heaven_destiny_moment.common.moment.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.ClientSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.RaidInstance;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class RaidMoment extends DefaultMoment {
    public static final MapCodec<RaidMoment> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DefaultMoment.CODEC.forGetter(raidMoment -> raidMoment),
            Codec.INT.optionalFieldOf("readyTime",100).forGetter(RaidMoment::readyTime)
    ).apply(instance, RaidMoment::new));


    private final int readyTime;

    public RaidMoment(DefaultMoment moment, int readyTime) {
        super(moment.barRenderType, moment.momentData, moment.tipSettings, moment.clientSettings, moment.trackers);
        this.readyTime = readyTime;
    }

    @Override
    public MomentInstance newMomentInstance(Level level, IMoment moment) {
        return new RaidInstance(level,moment);
    }

    public int readyTime() {
        return readyTime;
    }

    @Override
    public MapCodec<RaidMoment> codec() {
        return CODEC;
    }

    public static class Builder extends MomentBuilder<RaidMoment> {
        protected int readyTime = 100;

        public Builder readyTime(int readyTime) {
            this.readyTime = readyTime;
            return this;
        }

        @Override
        public RaidMoment build() {
            return new RaidMoment(
                    new DefaultMoment(
                        Optional.ofNullable(barRenderType),
                        Optional.ofNullable(momentData),
                        Optional.ofNullable(tipSettings),
                        Optional.ofNullable(clientSettings),
                        Optional.ofNullable(trackers)
                    ),
                    readyTime
            );
        }
    }
}
