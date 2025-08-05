package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.context.*;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.IReward;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MomentInstanceCacheProvider {
    public final Moment moment;

    //TipSettings
    private TipSettings tipSettings;



    //momentData
    private List<IReward> rewards;
    private EntityTypeScoreTable entityTypeScoreTable;
    private AutoActuatorGroupSettings autoActuatorGroupSettings;
    private EntitySpawnSettings entitySpawnSettings;

    //EntitySpawnSettings
    private List<Weighted<List<IEntityInfo>>> entitySpawnList;
    private BiomeEntitySpawnSettings biomeEntitySpawnSettings;
    private MobSpawnRule mobSpawnRule;
    private ISpawnAlgorithm spawnAlgorithm;
    private Boolean isAfterEndClearMonster;

    //BiomeEntitySpawnSettings
    private Map<MobCategory, SpawnCategoryMultiplierModifier> spawnCategoryMultiplierMap;
    private MobSpawnSettings biomeMobSpawnSettings;
    private EntitySpawnList entitySpawnListContext;



    public MomentInstanceCacheProvider(Moment moment) {
        this.moment = moment;
    }

    public void iniCache() {
        moment.tipSettings.ifPresent(tipSettings -> {
            this.tipSettings = tipSettings;
        });

        moment.momentData().ifPresent(momentData -> {
            momentData.entityTypeScoreTable().ifPresent(scoreTable -> {
                this.entityTypeScoreTable = scoreTable;
            });

            momentData.rewards().ifPresent(rewards -> {
                this.rewards = rewards;
            });

            momentData.autoActuatorGroupSettings().ifPresent(settings -> {
                this.autoActuatorGroupSettings = settings;
            });

            momentData.entitySpawnSettings().ifPresent(entitySpawnSettings -> {
                this.entitySpawnSettings = entitySpawnSettings;
                entitySpawnSettings.biomeEntitySpawnSettings().ifPresent(biomeEntitySpawnSettings -> {
                    this.biomeEntitySpawnSettings = biomeEntitySpawnSettings;
                    biomeEntitySpawnSettings.spawnCategoryMultiplier().ifPresent(spawnCategoryMultiplierMap -> {
                        this.spawnCategoryMultiplierMap = spawnCategoryMultiplierMap;
                    });
                    biomeEntitySpawnSettings.biomeMobSpawnSettings().ifPresent(biomeMobSpawnSettings -> {
                        this.biomeMobSpawnSettings = biomeMobSpawnSettings;
                    });
                    biomeEntitySpawnSettings.entitySpawnListContext().ifPresent(entitySpawnListContext -> {
                        this.entitySpawnListContext = entitySpawnListContext;
                    });
                });
                entitySpawnSettings.entitySpawnList().ifPresent(entitySpawnList -> {
                    this.entitySpawnList = entitySpawnList;
                });
                entitySpawnSettings.rule().ifPresent(rule -> {
                    this.mobSpawnRule = rule;
                });
                entitySpawnSettings.spawnAlgorithm().ifPresent(spawnAlgorithm -> {
                    this.spawnAlgorithm = spawnAlgorithm;
                });
                this.isAfterEndClearMonster = entitySpawnSettings.isAfterEndClearMonster();
            });
        });
    }

    public TipSettings getTipSettings() {
        return tipSettings;
    }

    public List<IReward> getRewards() {
        return rewards;
    }

    public EntityTypeScoreTable getEntityTypeScoreTable() {
        return entityTypeScoreTable == null ? new EntityTypeScoreTable.Builder().build() : entityTypeScoreTable;
    }

    public AutoActuatorGroupSettings getAutoActuatorGroupSettings() {
        return autoActuatorGroupSettings;
    }

    public EntitySpawnSettings getEntitySpawnSettings() {
        return entitySpawnSettings;
    }

    public List<Weighted<List<IEntityInfo>>> getEntitySpawnList() {
        return entitySpawnList;
    }

    public BiomeEntitySpawnSettings getBiomeEntitySpawnSettings() {
        return biomeEntitySpawnSettings;
    }

    public MobSpawnRule getMobSpawnRule() {
        return mobSpawnRule;
    }

    public ISpawnAlgorithm getSpawnAlgorithm() {
        return spawnAlgorithm;
    }

    public Boolean getAfterEndClearMonster() {
        return isAfterEndClearMonster;
    }

    public Map<MobCategory, SpawnCategoryMultiplierModifier> getSpawnCategoryMultiplierMap() {
        return spawnCategoryMultiplierMap;
    }

    public MobSpawnSettings getBiomeMobSpawnSettings() {
        return biomeMobSpawnSettings;
    }

    public EntitySpawnList getEntitySpawnListContext() {
        return entitySpawnListContext;
    }
}
