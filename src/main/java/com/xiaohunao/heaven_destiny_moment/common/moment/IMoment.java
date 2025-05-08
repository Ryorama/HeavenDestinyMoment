package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.function.Function;

public interface IMoment {
    Codec<Moment> CODEC = HDMRegistries.MOMENT_CODEC.byNameCodec().dispatch(new Function<Moment, MapCodec<? extends Moment>>() {
        @Override
        public MapCodec<? extends Moment> apply(Moment moment) {
            return moment.codec();
        }
    }, Function.identity());


    MapCodec<? extends Moment> codec();

    Optional<IBarRenderType> barRenderType();
//
//    CompoundTag serializeNBT();
//
//    void deserializeNBT(CompoundTag compoundTag);
}
