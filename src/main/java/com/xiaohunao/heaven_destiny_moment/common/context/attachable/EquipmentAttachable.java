package com.xiaohunao.heaven_destiny_moment.common.context.attachable;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.Weighted;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.IEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public record EquipmentAttachable(Weighted<Pair<IEquippableSlot, ItemStack>> equipments, Optional<Map<EquipmentSlot,Float>> canDropEquippable) implements IAttachable {
    public final static MapCodec<EquipmentAttachable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Weighted.codec(Codec.pair(IEquippableSlot.CODEC, ItemStack.CODEC)).fieldOf("equipments").forGetter(EquipmentAttachable::equipments),
            Codec.unboundedMap(EquipmentSlot.CODEC, Codec.FLOAT).optionalFieldOf("canDropEquippable").forGetter(EquipmentAttachable::canDropEquippable)
    ).apply(instance, EquipmentAttachable::new));


    @Override
    public void attachToEntity(LivingEntity livingEntity) {
        equipments.getRandomWeighted().forEach((pair -> pair.getFirst().wear(livingEntity, pair.getSecond())));


        canDropEquippable.ifPresent(canDropEquippable -> {
            canDropEquippable.forEach((slot, chance) -> {
                if (livingEntity instanceof Mob mob) {
                    mob.setDropChance(slot, chance);
                }
            });
        });
    }

    @Override
    public MapCodec<? extends IAttachable> codec() {
        return CODEC;
    }

    public static class Builder implements IBuilderConverter<EquipmentAttachable> {
        private Weighted.Builder<Pair<IEquippableSlot, ItemStack>> equipments;
        protected Map<EquipmentSlot, Float> canDropEquippable;

        public EquipmentAttachable build(){
            return new EquipmentAttachable(equipments.build(),Optional.ofNullable(canDropEquippable));
        }

        public Builder randomType(Weighted.RandomType randomType){
            equipments.randomType(randomType);
            return this;
        }

        public Builder addEquipment(IEquippableSlot slot, Item item) {
            return addEquipment(slot, item, 1);
        }

        public Builder addEquipment(IEquippableSlot slot, Item item, int weight) {
            if (equipments == null) {
                equipments = new Weighted.Builder<>();
            }
            equipments.add(new Pair<>(slot, item.getDefaultInstance()), weight);
            return this;
        }


//        public Builder addEnchantedTableEquipment(IEquippableSlot slot, Item item, int enchantLevel,HolderSet.Named<Enchantment> holders) {
//            return addEnchantedTableEquipment(slot,item,enchantLevel,1,holders);
//        }
//
//        public Builder addEnchantedTableEquipment(IEquippableSlot slot, Item item, int enchantLevel, int weight, HolderSet.Named<Enchantment> holders) {
//            if (equipments == null) {
//                equipments = new Weighted.Builder<>();
//            }
//
//            ItemStack itemStack = item.getDefaultInstance();
//            RandomSource random = RandomSource.create();
//
//            List<EnchantmentInstance> enchantments = EnchantmentHelper.selectEnchantment(
//                    random,
//                    itemStack,
//                    enchantLevel,
//                    holders.stream()
//            );
//
//            for (EnchantmentInstance enchantment : enchantments) {
//                itemStack.enchant(enchantment.enchantment, enchantment.level);
//            }
//
//            equipments.add(new Pair<>(slot, itemStack), weight);
//
//            return this;
//        }


//        public Builder addEnchantedEquipment(IEquippableSlot slot, Item item, Map<ResourceKey<Enchantment>,Integer> enchantments){
//            return addEnchantedEquipment(slot,item,1, enchantments);
//        }
//

//        public Builder addEnchantedEquipment(IEquippableSlot slot, Item item, int weight,Map<ResourceKey<Enchantment>,Integer> enchantments){
//            if (equipments == null) {
//                equipments = new Weighted.Builder<>();
//            }
//
//            ItemStack itemStack = item.getDefaultInstance();
//            for (Map.Entry<ResourceKey<Enchantment>, Integer> entry : enchantments.entrySet()) {
//                Optional<Holder.Reference<Enchantment>> enchantmentHolder = enchantmentRegistry.get(entry.getKey());
//                enchantmentHolder.ifPresent(holder -> {
//                    Enchantment enchantment = holder.value();
//                    if (enchantment.canEnchant(itemStack)) {
//                        itemStack.enchant(holder, entry.getValue());
//                    }
//                });
//            }
//
//            equipments.add(new Pair<>(slot, itemStack), weight);
//            return this;
//        }


        public Builder canDropEquippable(EquipmentSlot slot,float chance){
            if (this.canDropEquippable == null) {
                this.canDropEquippable = Maps.newHashMap();
            }
            this.canDropEquippable.put(slot, chance);
            return this;
        }

        @Override
        public Builder converter(EquipmentAttachable equipmentAttachable) {
            Builder builder = new Builder();
            builder.equipments = new Weighted.Builder<>();
            builder.equipments.converter(equipmentAttachable.equipments());
            equipmentAttachable.canDropEquippable().ifPresent(dropMap -> {
                if (builder.canDropEquippable == null) {
                    builder.canDropEquippable = Maps.newHashMap();
                }
                builder.canDropEquippable.putAll(dropMap);
            });
            return builder;
        }
    }
}
