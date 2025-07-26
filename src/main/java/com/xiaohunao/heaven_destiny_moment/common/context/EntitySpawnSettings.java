package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.EntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.OpenAreaSpawnAlgorithm;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.*;
import java.util.stream.Collectors;

public record EntitySpawnSettings(Optional<List<Weighted<List<IEntityInfo>>>> entitySpawnList, Optional<BiomeEntitySpawnSettings> biomeEntitySpawnSettings, Optional<MobSpawnRule> rule, Optional<ISpawnAlgorithm> spawnAlgorithm,Boolean isAfterEndClearMonster) {
    private static final Random RANDOM = new Random();

    public static final Codec<EntitySpawnSettings> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.list(Weighted.codec(Codec.list(IEntityInfo.CODEC))).optionalFieldOf("entity_spawn_list").forGetter(EntitySpawnSettings::entitySpawnList),
                    BiomeEntitySpawnSettings.CODEC.optionalFieldOf("biome_entity_Spawn_settings").forGetter(EntitySpawnSettings::biomeEntitySpawnSettings),
                    MobSpawnRule.CODEC.optionalFieldOf("spawn_rule").forGetter(EntitySpawnSettings::rule),
                    ISpawnAlgorithm.CODEC.optionalFieldOf("spawn_algorithm").forGetter(EntitySpawnSettings::spawnAlgorithm),
                    Codec.BOOL.optionalFieldOf("isAfterEndClearMonster",false).forGetter(EntitySpawnSettings::isAfterEndClearMonster)
            ).apply(builder, EntitySpawnSettings::new)
    );

    public List<Entity> spawnList(Level level, int wave){
        List<Entity> list = Lists.newArrayList();

        entitySpawnList.ifPresent(entitySpawnList -> {
            Weighted<List<IEntityInfo>> listWeighted = entitySpawnList.get(wave);

            listWeighted.getRandomWeighted().forEach(infoList-> {
                Weighted.Builder<IEntityInfo> builder = new Weighted.Builder<>();
                infoList.forEach(entityInfo -> {
                    if (entityInfo instanceof EntityInfo){
                        builder.add(entityInfo, ((EntityInfo) entityInfo).weight().orElse(1));
                    }
                });


                int sum = infoList.stream()
                        .filter(entityInfo -> entityInfo instanceof EntityInfo)
                        .mapToInt(entityInfo -> ((EntityInfo) entityInfo).weight().orElse(1))
                        .sum();

                for (IEntityInfo info : infoList) {
                    if (!(info instanceof EntityInfo entityInfo)) {
                        return;
                    }

                    int weight;
                    if (entityInfo.weight().isPresent()) {
                        weight = entityInfo.weight().get();
                    }else {
                        weight = 1;
                    }

                    if (RANDOM.nextInt(sum) < weight) {
                        list.addAll(entityInfo.spawn(level));
                    }
                }
            });
        });
        return list;
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> adjustmentBiomeEntitySpawnSettings(Moment moment,MobCategory mobCategory, List<MobSpawnSettings.SpawnerData> originalSpawnerData) {
        List<MobSpawnSettings.SpawnerData> ownSpawnerDataList = Lists.newArrayList();
        for (MobSpawnSettings.SpawnerData originalSpawnerDatum : originalSpawnerData) {
            ownSpawnerDataList.add(BiomeEntitySpawnSettings.OwnSpawnerData.ofVanilla(originalSpawnerDatum));
        }


        biomeEntitySpawnSettings.flatMap(BiomeEntitySpawnSettings::biomeMobSpawnSettings)
                .map(mobSpawnSettings -> {
                    WeightedRandomList<MobSpawnSettings.SpawnerData> spawnerData = mobSpawnSettings.spawners.get(mobCategory);
                    return spawnerData != null ? new ArrayList<>(spawnerData.unwrap()) : new ArrayList<MobSpawnSettings.SpawnerData>();
                })
                .ifPresent(newSpawnerData -> {
                    List<MobSpawnSettings.SpawnerData> newOwnSpawnerDataList = Lists.newArrayList();
                    for (MobSpawnSettings.SpawnerData originalSpawnerDatum : newSpawnerData) {
                        newOwnSpawnerDataList.add(BiomeEntitySpawnSettings.OwnSpawnerData.of(originalSpawnerDatum,moment));
                    }


                    boolean allowOriginal = rule.flatMap(MobSpawnRule::allowOriginalBiomeSpawnSettings).orElse(true);
                    if (allowOriginal) {
                        mergeSpawnerData(ownSpawnerDataList, newOwnSpawnerDataList);
                    } else {
                        ownSpawnerDataList.clear();
                        ownSpawnerDataList.addAll(newOwnSpawnerDataList);
                    }
                });

        applyBlackOrWhiteListFilter(ownSpawnerDataList);

        return WeightedRandomList.create(ownSpawnerDataList);
    }

    private void mergeSpawnerData(List<MobSpawnSettings.SpawnerData> original, List<MobSpawnSettings.SpawnerData> newData) {
        Set<EntityType<?>> originalTypes = original.stream()
                .map(data -> data.type)
                .collect(Collectors.toSet());

        for (MobSpawnSettings.SpawnerData data : newData) {
            EntityType<?> newDataType = data.type;
            if (!originalTypes.contains(newDataType)) {
                original.add(data);
            } else {
                original.removeIf(d -> d.type.equals(newDataType));
                original.add(data);
            }
        }
    }

    private void applyBlackOrWhiteListFilter(List<MobSpawnSettings.SpawnerData> spawnerData) {
        biomeEntitySpawnSettings.flatMap(BiomeEntitySpawnSettings::entitySpawnListContext)
                .ifPresent(list -> {
                    Predicate<MobSpawnSettings.SpawnerData> filterPredicate = data ->
                            list.contains(data.type) && list.isBlackList().isPresent() && list.isBlackList().get();
                    spawnerData.removeIf(filterPredicate);
                });
    }

    public static class Builder {
        private List<Weighted<List<IEntityInfo>>> entitySpawnList;
        private BiomeEntitySpawnSettings biomeEntitySpawnSettings;
        private MobSpawnRule rule;
        private ISpawnAlgorithm spawnAlgorithm = OpenAreaSpawnAlgorithm.DEFAULT;
        private boolean isAfterEndClearMonster = false;

        public Builder biomeEntitySpawnSettings(Function<BiomeEntitySpawnSettings.Builder,BiomeEntitySpawnSettings.Builder> biomeEntitySpawnSettings) {
            this.biomeEntitySpawnSettings = biomeEntitySpawnSettings.apply(new BiomeEntitySpawnSettings.Builder()).build();
            return this;
        }

        public Builder entitySpawnList(Weighted.RandomType randomType, Function<Weighted.Builder<List<IEntityInfo>>, Weighted.Builder<List<IEntityInfo>>> weightedEntityInfo) {
            if (entitySpawnList == null){
                entitySpawnList = Lists.newArrayList();
            }
            Weighted.Builder<List<IEntityInfo>> builder = new Weighted.Builder<>();
            builder.randomType(randomType);

            Collections.addAll(entitySpawnList, weightedEntityInfo.apply(builder).build());
            return this;
        }

        public Builder entitySpawnList(Function<Weighted.Builder<List<IEntityInfo>>, Weighted.Builder<List<IEntityInfo>>> weightedEntityInfo) {
            return entitySpawnList(Weighted.RandomType.ALL, weightedEntityInfo);
        }


        public Builder rule(Function<MobSpawnRule.Builder,MobSpawnRule.Builder> rule){
            this.rule = rule.apply(new MobSpawnRule.Builder()).build();
            return this;
        }

        public Builder spawnAlgorithm(ISpawnAlgorithm spawnAlgorithm){
            this.spawnAlgorithm = spawnAlgorithm;
            return this;
        }

        public Builder afterEndClearMonster(){
            this.isAfterEndClearMonster = true;
            return this;
        }


        public EntitySpawnSettings build() {
            return new EntitySpawnSettings(
                    Optional.ofNullable(entitySpawnList),
                    Optional.ofNullable(biomeEntitySpawnSettings),
                    Optional.ofNullable(rule),
                    Optional.ofNullable(spawnAlgorithm),
                    isAfterEndClearMonster
            );
        }
    }


}
