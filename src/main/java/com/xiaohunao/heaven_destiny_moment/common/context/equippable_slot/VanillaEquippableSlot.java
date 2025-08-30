package com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record VanillaEquippableSlot(EquipmentSlot slot) implements IEquippableSlot{
    public static final VanillaEquippableSlot MAINHAND = new VanillaEquippableSlot(EquipmentSlot.MAINHAND);
    public static final VanillaEquippableSlot OFFHAND = new VanillaEquippableSlot(EquipmentSlot.OFFHAND);
    public static final VanillaEquippableSlot HEAD = new VanillaEquippableSlot(EquipmentSlot.HEAD);
    public static final VanillaEquippableSlot CHEST = new VanillaEquippableSlot(EquipmentSlot.CHEST);
    public static final VanillaEquippableSlot LEGS = new VanillaEquippableSlot(EquipmentSlot.LEGS);
    public static final VanillaEquippableSlot FEET = new VanillaEquippableSlot(EquipmentSlot.FEET);
    public static final VanillaEquippableSlot BODY = new VanillaEquippableSlot(EquipmentSlot.BODY);



    public static final MapCodec<VanillaEquippableSlot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EquipmentSlot.CODEC.fieldOf("slot").forGetter(VanillaEquippableSlot::slot)
    ).apply(instance, VanillaEquippableSlot::new));


    public static VanillaEquippableSlot of(EquipmentSlot slot) {
        return new VanillaEquippableSlot(slot);
    }


    @Override
    public MapCodec<? extends IEquippableSlot> codec() {
        return CODEC;
    }

    @Override
    public void wear(LivingEntity livingEntity, ItemStack stack) {
        livingEntity.setItemSlot(slot,stack);
    }
}
