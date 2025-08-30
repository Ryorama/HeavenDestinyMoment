package com.xiaohunao.heaven_destiny_moment.common.context.entity_info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;


public class SlimeInfo extends EntityInfo{
    public static final MapCodec<SlimeInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(EntityInfo::entityType),
            IAmount.CODEC.optionalFieldOf("amount").forGetter(EntityInfo::amount),
            Codec.INT.optionalFieldOf("weight").forGetter(EntityInfo::weight),
            IAttachable.CODEC.listOf().optionalFieldOf("attaches").forGetter(EntityInfo::attaches),
            IEntityInfo.CODEC.optionalFieldOf("vehicle").forGetter(EntityInfo::vehicle),
            Codec.INT.optionalFieldOf("portal_cooldown").forGetter(EntityInfo::portal_cooldown),
            Codec.INT.fieldOf("size").forGetter(SlimeInfo::size)
    ).apply(instance, SlimeInfo::new));
    private final Integer size;

    public SlimeInfo(EntityType<?> entityType, Optional<IAmount> amount, Optional<Integer> weight, Optional<List<IAttachable>> attaches, Optional<IEntityInfo> vehicle,Optional<Integer> portal_cooldown, Integer size) {
        super(entityType, amount, weight, attaches, vehicle,portal_cooldown);
        this.size = size;
    }

    @Override
    public List<Entity> spawn(Level level) {
        List<Entity> spawn = super.spawn(level);
        spawn.forEach(entity -> {
            if (entity instanceof Slime slime){
                slime.setSize(size,false);
            }
        });
        return spawn;
    }

    @Override
    public MapCodec<? extends IEntityInfo> codec() {
        return CODEC;
    }

    public Integer size() {
        return size;
    }

    public static class Builder extends EntityInfo.Builder {
        private Integer size = 1;

        public Builder(EntityType<?> entityType) {
            super(entityType);
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        @Override
        public IEntityInfo build() {
            return new SlimeInfo(entityType, Optional.ofNullable(amount), Optional.ofNullable(weight), Optional.ofNullable(attaches),
                    Optional.ofNullable(vehicle),Optional.ofNullable(portal_cooldown), size);
        }
    }
}
