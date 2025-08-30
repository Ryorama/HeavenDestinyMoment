package com.xiaohunao.heaven_destiny_moment.common.context;


import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.IReward;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record MomentData(Optional<List<IReward>> rewards, Optional<AutoActuatorGroupSettings> autoActuatorGroupSettings,
                         Optional<EntitySpawnSettings> entitySpawnSettings,Optional<EntityTypeScoreTable> entityTypeScoreTable) {

    public static final Codec<MomentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(IReward.CODEC).optionalFieldOf("rewards").forGetter(MomentData::rewards),
            AutoActuatorGroupSettings.CODEC.optionalFieldOf("state_settings_group").forGetter(MomentData::autoActuatorGroupSettings),
            EntitySpawnSettings.CODEC.optionalFieldOf("entity_spawn_settings").forGetter(MomentData::entitySpawnSettings),
            EntityTypeScoreTable.CODEC.optionalFieldOf("entity_type_score_table").forGetter(MomentData::entityTypeScoreTable)

    ).apply(instance, MomentData::new));
    public static final MomentData EMPTY = new MomentData(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());


    public static class Builder implements IBuilderConverter<MomentData>{
        private List<IReward> rewards;
        private AutoActuatorGroupSettings autoActuatorGroupSettings;
        private EntitySpawnSettings entitySpawnSettings;
        private EntityTypeScoreTable entityTypeScoreTable;

        @Override
        public Builder converter(MomentData momentData) {
            Builder builder = new Builder();
            momentData.rewards.ifPresent(rewards -> builder.rewards = rewards);
            momentData.autoActuatorGroupSettings.ifPresent(settings -> builder.autoActuatorGroupSettings = settings);
            momentData.entitySpawnSettings.ifPresent(settings -> builder.entitySpawnSettings = settings);
            momentData.entityTypeScoreTable.ifPresent(scoreTable -> builder.entityTypeScoreTable = scoreTable);
            return builder;
        }


        public MomentData build() {
            return new MomentData(Optional.ofNullable(rewards),Optional.ofNullable(autoActuatorGroupSettings),Optional.ofNullable(entitySpawnSettings),Optional.ofNullable(entityTypeScoreTable));
        }

        public Builder addReward(IReward... reward) {
            if (rewards == null){
                rewards = Lists.newArrayList();
            }
            rewards.addAll(Set.of(reward));
            return this;
        }

        public Builder autoActuatorGroupSettings(Function<AutoActuatorGroupSettings.Builder, AutoActuatorGroupSettings.Builder> autoActuatorGroupSettings){
            AutoActuatorGroupSettings.Builder builder = new AutoActuatorGroupSettings.Builder();
            if (this.autoActuatorGroupSettings != null) {
                builder = builder.converter(this.autoActuatorGroupSettings);
            }
            this.autoActuatorGroupSettings = autoActuatorGroupSettings.apply(builder).build();
            return this;
        }


        public Builder entitySpawnSettings(Function<EntitySpawnSettings.Builder, EntitySpawnSettings.Builder> entitySpawnSettings){
            EntitySpawnSettings.Builder builder = new EntitySpawnSettings.Builder();
            if (this.entitySpawnSettings != null) {
                builder = builder.converter(this.entitySpawnSettings);
            }
            this.entitySpawnSettings = entitySpawnSettings.apply(builder).build();
            return this;
        }


        public Builder entityTypeScoreTable(Function<EntityTypeScoreTable.Builder,EntityTypeScoreTable.Builder> entityTypeScoreTable) {
            EntityTypeScoreTable.Builder builder = new EntityTypeScoreTable.Builder();
            if (this.entityTypeScoreTable != null) {
                builder = builder.converter(this.entityTypeScoreTable);
            }
            this.entityTypeScoreTable = entityTypeScoreTable.apply(builder).build();
            return this;
        }
    }
}
