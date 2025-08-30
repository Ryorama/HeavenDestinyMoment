package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public record EntityTypeScoreTable(Map<EntityType<?>,Integer> killType) {
    public static final Codec<EntityTypeScoreTable> CODEC = Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(),Codec.INT).xmap(EntityTypeScoreTable::new, EntityTypeScoreTable::killType);


    public Integer get(EntityType<?> entityType){
        return killType.getOrDefault(entityType,0);
    }

    public static class Builder implements IBuilderConverter<EntityTypeScoreTable> {
        private final Map<EntityType<?>,Integer> killType = Maps.newHashMap();

        public EntityTypeScoreTable build() {
            return new EntityTypeScoreTable(killType);
        }

        public Builder addType(EntityType<?> entityType, int integral){
            killType.put(entityType,integral);
            return this;
        }

        @Override
        public Builder converter(EntityTypeScoreTable entityTypeScoreTable) {
            Builder builder = new Builder();
            builder.killType.putAll(entityTypeScoreTable.killType);
            return builder;
        }
    }
}
