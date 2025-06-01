package com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public interface IEquippableSlot {
    Codec<IEquippableSlot> CODEC = Codec.lazyInitialized(HDMRegistries.EQUIPPABLE_SLOT_CODEC::byNameCodec).dispatch(IEquippableSlot::codec, Function.identity());

    MapCodec<? extends IEquippableSlot> codec();

    void wear(LivingEntity livingEntity,ItemStack stack);
}
