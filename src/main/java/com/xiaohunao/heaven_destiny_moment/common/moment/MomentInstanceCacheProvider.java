package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntityTypeScoreTable;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.IReward;

import java.util.List;
import java.util.Optional;

public class MomentInstanceCacheProvider {
    public final Moment moment;

    //momentData
    private List<IReward> rewards;
    private EntityTypeScoreTable entityTypeScoreTable;
    private AutoActuatorGroupSettings autoActuatorGroupSettings;
    private EntitySpawnSettings entitySpawnSettings;



    public MomentInstanceCacheProvider(Moment moment) {
        this.moment = moment;
    }

    public void iniCache() {
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

            momentData.entitySpawnSettings().ifPresent(settings -> {
                this.entitySpawnSettings = settings;
            });
        });
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
}
