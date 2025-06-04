package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.network.KillRequiredSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public record KillEntityCondition(KillEntityRecorderAttachment.KillType killType,
                                  Optional<Integer> requiredTotalCount,
                                  Optional<Integer> requiredTotalScore,
                                  Optional<Map<EntityType<?>, Integer>> requiredKillCounts,
                                  Optional<Map<EntityType<?>, Integer>> requiredKillScores,
                                  Optional<BiFunction<Integer, Difficulty, Integer>> difficultyScaling,
                                  Optional<BiFunction<Integer, Integer, Integer>> playerCountScaling) implements ICondition {

    public static final MapCodec<KillEntityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KillEntityRecorderAttachment.KillType.CODEC.fieldOf("kill_type").forGetter(KillEntityCondition::killType),
            Codec.INT.optionalFieldOf("required_total_count").forGetter(KillEntityCondition::requiredTotalCount),
            Codec.INT.optionalFieldOf("required_total_score").forGetter(KillEntityCondition::requiredTotalScore),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).optionalFieldOf("required_kill_counts").forGetter(KillEntityCondition::requiredKillCounts),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).optionalFieldOf("required_kill_scores").forGetter(KillEntityCondition::requiredKillScores),
            HDMRegistries.DIFFICULTY_SCALING.byNameCodec().optionalFieldOf("difficulty_scaling").forGetter(KillEntityCondition::difficultyScaling),
            HDMRegistries.PLAYER_COUNT_SCALING.byNameCodec().optionalFieldOf("player_count_scaling").forGetter(KillEntityCondition::playerCountScaling)
    ).apply(instance, KillEntityCondition::new));

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        KillEntityRecorderAttachment killEntityRecorderAttachment = null;
        if (killType == KillEntityRecorderAttachment.KillType.MOMENT){
            killEntityRecorderAttachment = instance.getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER);
        }else {
            if (serverPlayer != null) {
                killEntityRecorderAttachment = serverPlayer.getData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER);
            }
        }

        if (killEntityRecorderAttachment == null) {
            return false;
        }


        RequiredKill requiredKill = getKillRecord(instance.getLevel());

        int totalKills = killEntityRecorderAttachment.getTotalKills();
        int totalScore = killEntityRecorderAttachment.getTotalScore();
        Map<EntityType<?>, Integer> entityTypeKills = killEntityRecorderAttachment.getEntityTypeKills();
        Map<EntityType<?>, Integer> entityTypeScores = killEntityRecorderAttachment.getEntityTypeScores();



        boolean matches = true;

        // 检查总击杀数是否满足要求
        if (requiredTotalCount.isPresent() && totalKills < requiredKill.totalKills()) {
            matches = false;
        }

        // 检查总分数是否满足要求
        if (requiredTotalScore.isPresent() && totalScore < requiredKill.totalScore()) {
            matches = false;
        }

        // 检查每种实体类型的击杀数是否满足要求
        for (Map.Entry<EntityType<?>, Integer> entry : requiredKill.entityTypeKills().entrySet()) {
            EntityType<?> entityType = entry.getKey();
            Integer requiredCount = entry.getValue();

            int actualCount = entityTypeKills.getOrDefault(entityType, 0);
            if (actualCount < requiredCount) {
                matches = false;
                break;
            }
        }

        // 检查每种实体类型的得分是否满足要求
        for (Map.Entry<EntityType<?>, Integer> entry : requiredKill.entityTypeScores().entrySet()) {
            EntityType<?> entityType = entry.getKey();
            int requiredScore = entry.getValue();

            int actualScore = entityTypeScores.getOrDefault(entityType, 0);
            if (actualScore < requiredScore) {
                matches = false;
            }
        }

        if (killType == KillEntityRecorderAttachment.KillType.MOMENT){
            //TODO: requiredKill
//            instance.setVictoryRequiredKill(requiredKill);
//            PacketDistributor.sendToAllPlayers(new KillRequiredSyncPayload(instance.getID(),requiredKill));
        }

        // 所有条件都满足
        return matches;
    }

    public RequiredKill getKillRecord(Level level) {
        // 应用难度和玩家数量缩放
        int scaledRequiredTotalCount = getScaledValue(requiredTotalCount, level);
        int scaledRequiredTotalScore = getScaledValue(requiredTotalScore, level);
        Map<EntityType<?>, Integer> scaledRequiredKillCounts = Maps.newHashMap();
        if (requiredKillCounts.isPresent()) {
            scaledRequiredKillCounts = new HashMap<>();
            for (Map.Entry<EntityType<?>, Integer> entry : requiredKillCounts.get().entrySet()) {
                EntityType<?> entityType = entry.getKey();
                scaledRequiredKillCounts.put(entityType, getScaledValue(Optional.of(entry.getValue()), level));
            }
        }

        Map<EntityType<?>, Integer> scaledRequiredKillScores = Maps.newHashMap();
        if (requiredKillScores.isPresent()) {
            scaledRequiredKillScores = new HashMap<>();
            for (Map.Entry<EntityType<?>, Integer> entry : requiredKillScores.get().entrySet()) {
                EntityType<?> entityType = entry.getKey();
                scaledRequiredKillScores.put(entityType, getScaledValue(Optional.of(entry.getValue()), level));
            }
        }

        // 返回计算后的击杀记录
        return new RequiredKill(scaledRequiredTotalCount, scaledRequiredTotalScore, scaledRequiredKillCounts, scaledRequiredKillScores);
    }


    /**
     * 根据难度和玩家数量缩放获得实际需要的值
     */
    private int getScaledValue(Optional<Integer> baseValue, Level level) {
        if (baseValue.isEmpty()) {
            return baseValue.orElse(0);
        }
        
        int value = baseValue.get();
        Difficulty difficulty = level.getDifficulty();
        int playerCount = level.players().size();

        // 应用难度缩放
        if (difficultyScaling.isPresent()) {
            value = difficultyScaling.get().apply(value, difficulty);
        }


        
        // 应用玩家数量缩放
        if (playerCountScaling.isPresent()) {
            value = playerCountScaling.get().apply(value, playerCount);
        }
        
        return value;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.KILL_ENTITY_CONDITION.get();
    }

    public static KillEntityCondition.Builder builder(KillEntityRecorderAttachment.KillType killType) {
        return new Builder(killType);
    }


    public static class Builder {
        private final KillEntityRecorderAttachment.KillType killType;
        private Integer requiredTotalCount = null;
        private Integer requiredTotalScore = null;
        private Map<EntityType<?>, Integer> requiredKillCounts = null;
        private Map<EntityType<?>, Integer> requiredKillScores = null;
        private BiFunction<Integer, Difficulty, Integer> difficultyScaling = null;
        private BiFunction<Integer, Integer, Integer> playerCountScaling = null;


        public Builder(KillEntityRecorderAttachment.KillType killType) {
            this.killType = killType;
        }


        /**
         * 设置所需的总击杀数
         * @param count 总击杀数
         * @return this builder
         */
        public Builder withRequiredTotalCount(int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("Required total count must be positive");
            }
            this.requiredTotalCount = count;
            return this;
        }

        /**
         * 设置所需的总分数
         * @param score 总分数
         * @return this builder
         */
        public Builder withRequiredTotalScore(int score) {
            if (score <= 0) {
                throw new IllegalArgumentException("Required total score must be positive");
            }
            this.requiredTotalScore = score;
            return this;
        }

        /**
         * 添加特定实体类型的击杀数要求
         * @param entityType 实体类型
         * @param count 击杀数
         * @return this builder
         */
        public Builder withRequiredKillCount(EntityType<?> entityType, int count) {
            if (count <= 0) {
                throw new IllegalArgumentException("Required kill count must be positive");
            }
            if (this.requiredKillCounts == null) {
                this.requiredKillCounts = new HashMap<>();
            }
            this.requiredKillCounts.put(entityType, count);
            return this;
        }

        /**
         * 添加特定实体类型的分数要求
         * @param entityType 实体类型
         * @param score 分数
         * @return this builder
         */
        public Builder withRequiredKillScore(EntityType<?> entityType, int score) {
            if (score <= 0) {
                throw new IllegalArgumentException("Required kill score must be positive");
            }
            if (this.requiredKillScores == null) {
                this.requiredKillScores = new HashMap<>();
            }
            this.requiredKillScores.put(entityType, score);
            return this;
        }

        /**
         * 设置难度缩放函数的ID
         * @param difficultyScaling 难度缩放函数
         * @return this builder
         */
        public Builder withDifficultyScaling(BiFunction<Integer, Difficulty, Integer> difficultyScaling) {
            this.difficultyScaling = difficultyScaling;
            return this;
        }

        /**
         * 设置玩家数量缩放函数的ID
         * @param playerCountScaling 玩家数量缩放函数
         * @return this builder
         */
        public Builder withPlayerCountScaling(BiFunction<Integer, Integer, Integer> playerCountScaling) {
            this.playerCountScaling = playerCountScaling;
            return this;
        }

        /**
         * 直接设置所有需要的特定实体类型的击杀数要求
         * @param killCounts 实体类型及对应击杀数的映射
         * @return this builder
         */
        public Builder withRequiredKillCounts(Map<EntityType<?>, Integer> killCounts) {
            if (killCounts == null) {
                this.requiredKillCounts = null;
                return this;
            }

            for (Map.Entry<EntityType<?>, Integer> entry : killCounts.entrySet()) {
                if (entry.getValue() <= 0) {
                    throw new IllegalArgumentException("Required kill count must be positive");
                }
            }

            this.requiredKillCounts = new HashMap<>(killCounts);
            return this;
        }

        /**
         * 直接设置所有需要的特定实体类型的分数要求
         * @param killScores 实体类型及对应分数的映射
         * @return this builder
         */
        public Builder withRequiredKillScores(Map<EntityType<?>, Integer> killScores) {
            if (killScores == null) {
                this.requiredKillScores = null;
                return this;
            }

            for (Map.Entry<EntityType<?>, Integer> entry : killScores.entrySet()) {
                if (entry.getValue() <= 0) {
                    throw new IllegalArgumentException("Required kill score must be positive");
                }
            }

            this.requiredKillScores = new HashMap<>(killScores);
            return this;
        }

        /**
         * 构建KillEntityCondition实例
         * @return 新的KillEntityCondition实例
         */
        public KillEntityCondition build() {
            if (killType == null) {
                throw new IllegalStateException("KillType must be specified");
            }

            return new KillEntityCondition(
                    killType,
                    Optional.ofNullable(requiredTotalCount),
                    Optional.ofNullable(requiredTotalScore),
                    Optional.ofNullable(requiredKillCounts),
                    Optional.ofNullable(requiredKillScores),
                    Optional.ofNullable(difficultyScaling),
                    Optional.ofNullable(playerCountScaling)
            );
        }
    }

    public record RequiredKill(int totalKills, int totalScore, Map<EntityType<?>, Integer> entityTypeKills, Map<EntityType<?>, Integer> entityTypeScores) {
        public static final Codec<RequiredKill> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("totalKills").forGetter(RequiredKill::totalKills),
                Codec.INT.fieldOf("totalScore").forGetter(RequiredKill::totalScore),
                Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).fieldOf("entityTypeKills").forGetter(RequiredKill::entityTypeKills),
                Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).fieldOf("entityTypeScores").forGetter(RequiredKill::entityTypeScores)
        ).apply(instance, RequiredKill::new));
    }
}
