package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.Map;
import java.util.Optional;

public record BiomeEntitySpawnSettings(Optional<MobSpawnSettings> biomeMobSpawnSettings, Optional<Map<MobCategory, SpawnCategoryMultiplierModifier>> spawnCategoryMultiplier, Optional<EntitySpawnList> entitySpawnListContext) {
    public static final Codec<BiomeEntitySpawnSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MobSpawnSettings.CODEC.codec().optionalFieldOf("biome_mob_spawn_settings").forGetter(BiomeEntitySpawnSettings::biomeMobSpawnSettings),
            Codec.unboundedMap(MobCategory.CODEC, SpawnCategoryMultiplierModifier.CODEC).optionalFieldOf("spawn_category_multiplier").forGetter(BiomeEntitySpawnSettings::spawnCategoryMultiplier),
            EntitySpawnList.CODEC.optionalFieldOf("entitySpawnListContext").forGetter(BiomeEntitySpawnSettings::entitySpawnListContext)
    ).apply(builder, BiomeEntitySpawnSettings::new));

    public static class Builder {
        private MobSpawnSettings biomeMobSpawnSettings;
        private Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplier;
        private EntitySpawnList entitySpawnList;

        public BiomeEntitySpawnSettings build() {
            return new BiomeEntitySpawnSettings(Optional.ofNullable(biomeMobSpawnSettings), Optional.ofNullable(spawnCategoryMultiplier), Optional.ofNullable(entitySpawnList));
        }

        public Builder biomeMobSpawnSettings(Function<MobSpawnSettings.Builder, MobSpawnSettings.Builder> biomeMobSpawnSettings) {
            this.biomeMobSpawnSettings = biomeMobSpawnSettings.apply(new MobSpawnSettings.Builder()).build();
            return this;
        }

        public Builder spawnCategoryMultiplier(MobCategory category, SpawnCategoryMultiplierModifier multiplier) {
            if (spawnCategoryMultiplier == null) {
                this.spawnCategoryMultiplier = Maps.newHashMap();
            }
            this.spawnCategoryMultiplier.put(category, multiplier);
            return this;
        }

        public Builder entitySpawnListContext(Function<EntitySpawnList.Builder, EntitySpawnList.Builder> entitySpawnListContext) {
            this.entitySpawnList = entitySpawnListContext.apply(new EntitySpawnList.Builder()).build();
            return this;
        }
    }
}
