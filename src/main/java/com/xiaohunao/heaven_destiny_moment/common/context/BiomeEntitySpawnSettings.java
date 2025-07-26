package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.*;

public record BiomeEntitySpawnSettings(Optional<OwnMobSpawnSettings> biomeMobSpawnSettings, Optional<Map<MobCategory, SpawnCategoryMultiplierModifier>> spawnCategoryMultiplier, Optional<EntitySpawnList> entitySpawnListContext) {
    public static final Codec<BiomeEntitySpawnSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            OwnMobSpawnSettings.CODEC.codec().optionalFieldOf("biome_mob_spawn_settings").forGetter(BiomeEntitySpawnSettings::biomeMobSpawnSettings),
            Codec.unboundedMap(MobCategory.CODEC, SpawnCategoryMultiplierModifier.CODEC).optionalFieldOf("spawn_category_multiplier").forGetter(BiomeEntitySpawnSettings::spawnCategoryMultiplier),
            EntitySpawnList.CODEC.optionalFieldOf("entitySpawnListContext").forGetter(BiomeEntitySpawnSettings::entitySpawnListContext)
    ).apply(builder, BiomeEntitySpawnSettings::new));

    public static class Builder {
        private OwnMobSpawnSettings biomeMobSpawnSettings;
        private Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplier;
        private EntitySpawnList entitySpawnList;

        public BiomeEntitySpawnSettings build() {
            return new BiomeEntitySpawnSettings(Optional.ofNullable(biomeMobSpawnSettings), Optional.ofNullable(spawnCategoryMultiplier), Optional.ofNullable(entitySpawnList));
        }

        public Builder biomeMobSpawnSettings(Function<MobSpawnSettings.Builder, MobSpawnSettings.Builder> biomeMobSpawnSettings) {
            this.biomeMobSpawnSettings = new OwnMobSpawnSettings(biomeMobSpawnSettings.apply(new MobSpawnSettings.Builder()).build());
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

    public static class OwnMobSpawnSettings extends MobSpawnSettings {
        public static final MapCodec<OwnMobSpawnSettings> CODEC = MobSpawnSettings.CODEC.xmap(OwnMobSpawnSettings::new, java.util.function.Function.identity());
        private final Map<MobCategory, WeightedRandomList<OwnSpawnerData>> ownSpawners;

        public OwnMobSpawnSettings(float creatureGenerationProbability, Map<MobCategory, WeightedRandomList<OwnSpawnerData>> spawners, Map<EntityType<?>, MobSpawnCost> mobSpawnCosts) {
            super(creatureGenerationProbability, covertFrom(spawners), mobSpawnCosts);
            this.ownSpawners = spawners;
        }

        public OwnMobSpawnSettings(MobSpawnSettings settings) {
            super(settings.getCreatureProbability(), settings.spawners, settings.mobSpawnCosts);
            this.ownSpawners = covertTo(settings.spawners);
        }

        private static Map<MobCategory, WeightedRandomList<OwnSpawnerData>> covertTo(Map<MobCategory, WeightedRandomList<SpawnerData>> map) {
            Map<MobCategory, WeightedRandomList<OwnSpawnerData>> ret = new HashMap<>();
            for (Map.Entry<MobCategory, WeightedRandomList<SpawnerData>> entry : map.entrySet()) {
                ret.put(entry.getKey(), WeightedRandomList.create(entry.getValue().items.stream().map(OwnSpawnerData::new).toList()));
            }
            return ret;
        }

        private static Map<MobCategory, WeightedRandomList<SpawnerData>> covertFrom(Map<MobCategory, WeightedRandomList<OwnSpawnerData>> map) {
            Map<MobCategory, WeightedRandomList<SpawnerData>> ret = new HashMap<>();
            for (Map.Entry<MobCategory, WeightedRandomList<OwnSpawnerData>> entry : map.entrySet()) {
                ret.put(entry.getKey(), WeightedRandomList.create(new ArrayList<>(entry.getValue().items)));
            }
            return ret;
        }
    }

    public static class OwnSpawnerData extends MobSpawnSettings.SpawnerData {
        public static final Codec<OwnSpawnerData> CODEC = MobSpawnSettings.SpawnerData.CODEC.xmap(OwnSpawnerData::new, java.util.function.Function.identity());

        private Moment moment;

        public OwnSpawnerData(EntityType<?> type, int weight, int minCount, int maxCount) {
            super(type, weight, minCount, maxCount);
        }

        public OwnSpawnerData(EntityType<?> type, Weight weight, int minCount, int maxCount) {
            super(type, weight, minCount, maxCount);
        }

        public OwnSpawnerData(MobSpawnSettings.SpawnerData spawnerData) {
            super(spawnerData.type, spawnerData.getWeight(), spawnerData.minCount, spawnerData.maxCount);
        }

        public OwnSpawnerData(MobSpawnSettings.SpawnerData spawnerData, Moment moment) {
            this(spawnerData);
            this.moment = moment;
        }

        public static OwnSpawnerData of(MobSpawnSettings.SpawnerData spawnerData, Moment moment) {
            return new OwnSpawnerData(spawnerData, moment);
        }

        public static OwnSpawnerData ofVanilla(MobSpawnSettings.SpawnerData spawnerData) {
            return new OwnSpawnerData(spawnerData, HeavenDestinyMoment.EMITY_MOMENT);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this){
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }

            OwnSpawnerData that = (OwnSpawnerData) obj;
            return this.type == that.type &&
                    this.getWeight() == that.getWeight() &&
                    this.minCount == that.minCount &&
                    this.maxCount == that.maxCount;
        }

        public Moment getMoment() {
            return moment;
        }
    }
}
