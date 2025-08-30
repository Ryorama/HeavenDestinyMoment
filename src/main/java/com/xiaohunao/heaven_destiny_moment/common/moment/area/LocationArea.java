package com.xiaohunao.heaven_destiny_moment.common.moment.area;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.LocationCondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.function.Function;


public record LocationArea(LocationCondition locationCondition) implements Area {
    public static final MapCodec<LocationArea> CODEC = LocationCondition.CODEC.xmap(LocationArea::new, LocationArea::locationCondition);

    public static final LocationArea EMPTY = new LocationArea(LocationCondition.Builder.location().build());
    public static final LocationArea OVERWORLD = new Builder().build(builder -> builder.setDimension(Level.OVERWORLD).build());



    @Override
    public MapCodec<? extends Area> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(ServerLevel level, BlockPos pos) {
        return locationCondition.matches(level, pos);
    }

    public static class Builder {
        public LocationArea build(Function<LocationCondition.Builder, LocationCondition> function) {
            return new LocationArea(function.apply(new LocationCondition.Builder()));
        }
    }
}
