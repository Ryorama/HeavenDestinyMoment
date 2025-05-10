package com.xiaohunao.heaven_destiny_moment.common.attachment;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public final class MomentKillEntityRecorderAttachment {
    public static final Codec<MomentKillEntityRecorderAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).optionalFieldOf("entity_type_kills", Maps.newHashMap()).forGetter(MomentKillEntityRecorderAttachment::getEntityTypeKills),
            Codec.INT.optionalFieldOf("totalKills", 0).forGetter(MomentKillEntityRecorderAttachment::getTotalKills),
            Codec.INT.optionalFieldOf("totalScore", 0).forGetter(MomentKillEntityRecorderAttachment::getTotalScore),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).optionalFieldOf("entity_type_scores", Maps.newHashMap()).forGetter(MomentKillEntityRecorderAttachment::getEntityTypeScores)
    ).apply(instance, MomentKillEntityRecorderAttachment::new));
    
    private final Map<EntityType<?>, Integer> entityTypeKills;
    private int totalKills;
    private int totalScore;
    private final Map<EntityType<?>, Integer> entityTypeScores;

    public MomentKillEntityRecorderAttachment(Map<EntityType<?>, Integer> entityTypeKills, int totalKills, int totalScore, Map<EntityType<?>, Integer> entityTypeScores) {
        this.entityTypeKills = entityTypeKills;
        this.totalKills = totalKills;
        this.totalScore = totalScore;
        this.entityTypeScores = entityTypeScores;
    }

    public static MomentKillEntityRecorderAttachment create() {
        return new MomentKillEntityRecorderAttachment(Maps.newHashMap(), 0, 0, Maps.newHashMap());
    }

    public MomentKillEntityRecorderAttachment addKill(LivingEntity livingEntity, int score) {
        totalKills++;
        entityTypeKills.merge(livingEntity.getType(), 1, Integer::sum);
        
        totalScore += score;
        entityTypeScores.merge(livingEntity.getType(), score, Integer::sum);
        
        return this;
    }

    public Map<EntityType<?>, Integer> getEntityTypeKills() {
        return entityTypeKills;
    }

    public int getEntityKills(EntityType<?> entityType) {
        return entityTypeKills.getOrDefault(entityType, 0);
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public Map<EntityType<?>, Integer> getEntityTypeScores() {
        return entityTypeScores;
    }

    public int getEntityScore(EntityType<?> entityType) {
        return entityTypeScores.getOrDefault(entityType, 0);
    }
}
