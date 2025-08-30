package com.xiaohunao.heaven_destiny_moment.common.context;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

public record BiomeEntitySpawnSettings(Optional<MobSpawnSettings> biomeMobSpawnSettings, Optional<Map<MobCategory, SpawnCategoryMultiplierModifier>> spawnCategoryMultiplier, Optional<EntitySpawnList> entitySpawnListContext) {

    public static final Codec<BiomeEntitySpawnSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            MobSpawnSettings.CODEC.codec().optionalFieldOf("biome_mob_spawn_settings").forGetter(BiomeEntitySpawnSettings::biomeMobSpawnSettings),
            Codec.unboundedMap(MobCategory.CODEC, SpawnCategoryMultiplierModifier.CODEC).optionalFieldOf("spawn_category_multiplier").forGetter(BiomeEntitySpawnSettings::spawnCategoryMultiplier),
            EntitySpawnList.CODEC.optionalFieldOf("entitySpawnListContext").forGetter(BiomeEntitySpawnSettings::entitySpawnListContext)
    ).apply(builder, BiomeEntitySpawnSettings::new));

    public static class Builder implements IBuilderConverter<BiomeEntitySpawnSettings> {

        private MobSpawnSettings biomeMobSpawnSettings;
        private Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplier;
        private EntitySpawnList entitySpawnList;

        public BiomeEntitySpawnSettings build() {
            return new BiomeEntitySpawnSettings(Optional.ofNullable(biomeMobSpawnSettings), Optional.ofNullable(spawnCategoryMultiplier), Optional.ofNullable(entitySpawnList));
        }

        public Builder biomeMobSpawnSettings(Function<MobSpawnSettings.Builder, MobSpawnSettings.Builder> biomeMobSpawnSettings) {
            MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
            if (this.biomeMobSpawnSettings != null) {
                builder = mobSpawnSettingsConverter(this.biomeMobSpawnSettings);
            }
            this.biomeMobSpawnSettings = biomeMobSpawnSettings.apply(builder).build();
            return this;
        }

        public Builder biomeMobSpawnSettings(MobSpawnSettings biomeMobSpawnSettings) {
            this.biomeMobSpawnSettings = biomeMobSpawnSettings;
            return this;
        }

        public Builder spawnCategoryMultiplier(MobCategory category, SpawnCategoryMultiplierModifier multiplier) {
            if (spawnCategoryMultiplier == null) {
                this.spawnCategoryMultiplier = Maps.newHashMap();
            }
            this.spawnCategoryMultiplier.put(category, multiplier);
            return this;
        }

        public Builder spawnCategoryMultiplier(Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplier) {
            if (this.spawnCategoryMultiplier == null) {
                this.spawnCategoryMultiplier = Maps.newHashMap();
            }
            this.spawnCategoryMultiplier.putAll(spawnCategoryMultiplier);
            return this;
        }

        public Builder entitySpawnListContext(Function<EntitySpawnList.Builder, EntitySpawnList.Builder> entitySpawnListContext) {
            EntitySpawnList.Builder builder = new EntitySpawnList.Builder();
            if (this.entitySpawnList != null) {
                builder = builder.converter(this.entitySpawnList);
            }
            this.entitySpawnList = entitySpawnListContext.apply(builder).build();
            return this;
        }

        public Builder entitySpawnListContext(EntitySpawnList entitySpawnList) {
            this.entitySpawnList = entitySpawnList;
            return this;
        }

        @Override
        public Builder converter(BiomeEntitySpawnSettings biomeEntitySpawnSettings) {
            Builder builder = new Builder();
            biomeEntitySpawnSettings.biomeMobSpawnSettings.ifPresent(settings -> builder.biomeMobSpawnSettings = settings);
            biomeEntitySpawnSettings.spawnCategoryMultiplier.ifPresent(multiplier -> {
                if (builder.spawnCategoryMultiplier == null) {
                    builder.spawnCategoryMultiplier = Maps.newHashMap();
                }
                builder.spawnCategoryMultiplier.putAll(multiplier);
            });
            biomeEntitySpawnSettings.entitySpawnListContext.ifPresent(list -> builder.entitySpawnList = list);
            return builder;
        }
    }

    public static MobSpawnSettings.Builder mobSpawnSettingsConverter(MobSpawnSettings entitySpawnSettings) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        entitySpawnSettings.spawners.forEach((category, spawners) -> {
            spawners.unwrap().forEach(spawnerData -> builder.addSpawn(category, spawnerData));
        });
        entitySpawnSettings.mobSpawnCosts.forEach(((entityType, mobSpawnCost) -> {
            builder.addMobCharge(entityType, mobSpawnCost.charge(),mobSpawnCost.energyBudget());
        }));
        builder.creatureGenerationProbability(entitySpawnSettings.getCreatureProbability());
        return builder;
    }
}
