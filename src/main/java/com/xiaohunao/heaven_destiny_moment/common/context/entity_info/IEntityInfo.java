package com.xiaohunao.heaven_destiny_moment.common.context.entity_info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.xhn_lib.common.codec.ICodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Function;

public interface IEntityInfo extends ICodec<IEntityInfo> {
    Codec<IEntityInfo> CODEC = Codec.lazyInitialized(HDMRegistries.ENTITY_INFO_CODEC::byNameCodec).dispatch(IEntityInfo::codec, Function.identity());

    MapCodec<? extends IEntityInfo> codec();

    List<Entity> spawn(Level level);

}
