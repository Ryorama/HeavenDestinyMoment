package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.context.*;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.IReward;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.List;
import java.util.Map;

public class MomentInstanceCacheProvider {
    public final IMoment moment;

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


    //autoActuatorGroupSettings
    private AutomationRule createAutomationRule;
    private List<AutomationRule> runtimeAutomationRules;

    //trackers
    private List<ITracker> trackers;



    public MomentInstanceCacheProvider(IMoment moment) {
        this.moment = moment;
        iniCache();
    }

    public void iniCache() {
        moment.tipSettings().ifPresent(tipSettings -> {
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
                settings.createRule().ifPresent(rule -> {
                    this.createAutomationRule = rule;
                });
                settings.runtimeRules().ifPresent(list -> {
                    this.runtimeAutomationRules = list;
                });
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

        moment.trackers().ifPresent(trackers -> {
            this.trackers = trackers;
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

    public AutomationRule getCreateAutomationRule() {
        return createAutomationRule;
    }

    public List<AutomationRule> getRuntimeAutomationRules() {
        return runtimeAutomationRules;
    }

    public List<ITracker> getTrackers() {
        return trackers;
    }
}
