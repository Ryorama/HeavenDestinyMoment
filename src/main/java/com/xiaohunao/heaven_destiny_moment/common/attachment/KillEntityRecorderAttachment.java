package com.xiaohunao.heaven_destiny_moment.common.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public final class KillEntityRecorderAttachment {
    public static final Codec<KillEntityRecorderAttachment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            KillRecord.CODEC.listOf().fieldOf("killRecords").forGetter(KillEntityRecorderAttachment::getKillRecords),
            Codec.INT.fieldOf("totalKills").forGetter(KillEntityRecorderAttachment::getTotalKills),
            Codec.INT.fieldOf("totalScore").forGetter(KillEntityRecorderAttachment::getTotalScore),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).fieldOf("entityTypeKills").forGetter(KillEntityRecorderAttachment::getEntityTypeKills),
            Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), Codec.INT).fieldOf("entityTypeScores").forGetter(KillEntityRecorderAttachment::getEntityTypeScores)
    ).apply(instance, KillEntityRecorderAttachment::new));

    private List<KillRecord> killRecords;
    private int totalKills; //击杀总数
    private int totalScore; //击杀总分
    private Map<EntityType<?>, Integer> entityTypeKills; //按实体类型记录击杀数量
    private Map<EntityType<?>, Integer> entityTypeScores; //按实体类型记录击杀分数

    public KillEntityRecorderAttachment(List<KillRecord> killRecords, int totalKills, int totalScore, Map<EntityType<?>, Integer> entityTypeKills, Map<EntityType<?>, Integer> entityTypeScores) {
        this.killRecords = killRecords;
        this.totalKills = totalKills;
        this.totalScore = totalScore;
        this.entityTypeKills = entityTypeKills;
        this.entityTypeScores = entityTypeScores;
    }

    public static KillEntityRecorderAttachment create(){
        return new KillEntityRecorderAttachment(new CopyOnWriteArrayList<>(), 0, 0, new HashMap<>(), new HashMap<>());
    }



    public KillEntityRecorderAttachment addKill(LivingEntity livingEntity, DamageSource source, Integer score){
        long gameTime = livingEntity.level().getGameTime();
        EntityType<?> entityType = livingEntity.getType();
        KillRecord killRecord = KillRecord.of(entityType, gameTime, source, score);


        killRecords.add(killRecord);

        this.totalKills++;
        this.totalScore += score;
        this.entityTypeKills.merge(entityType, 1, Integer::sum);
        this.entityTypeScores.merge(entityType, score, Integer::sum);

        return this;
    }

    public List<KillRecord> getKillRecords() {
        return killRecords;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public Map<EntityType<?>, Integer> getEntityTypeKills() {
        return entityTypeKills;
    }

    public Map<EntityType<?>, Integer> getEntityTypeScores() {
        return entityTypeScores;
    }


    public record KillRecord(EntityType<?> entityType, long timestamp, int score, DamageType damageType,
                             Optional<EntityType<?>> causingEntity, Optional<EntityType<?>> directEntity,
                             Optional<Vec3> damageSourcePosition) {
        public static final Codec<KillRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entityType").forGetter(KillRecord::entityType),
                Codec.LONG.fieldOf("timestamp").forGetter(KillRecord::timestamp),
                Codec.INT.fieldOf("score").forGetter(KillRecord::score),
                DamageType.DIRECT_CODEC.fieldOf("damageType").forGetter(KillRecord::damageType),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("causingEntity").forGetter(KillRecord::causingEntity),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("directEntity").forGetter(KillRecord::directEntity),
                Vec3.CODEC.optionalFieldOf("damageSourcePosition").forGetter(KillRecord::damageSourcePosition)
        ).apply(instance, KillRecord::new));

        public static KillRecord of(EntityType<?> entityType,long timestamp, DamageSource damageSource, int score) {
            Entity causingEntity = damageSource.getEntity();
            Entity directEntity = damageSource.getDirectEntity();

            return new KillRecord(entityType,timestamp, score, damageSource.type(),
                    Optional.ofNullable(causingEntity == null ? null : causingEntity.getType()), Optional.ofNullable(directEntity == null ? null : directEntity.getType()),
                    Optional.ofNullable(damageSource.sourcePositionRaw()
                    )
            );
        }
    }

    public enum KillType implements StringRepresentable {
        MOMENT,
        PLAYER;

        public static final Codec<KillType> CODEC = StringRepresentable.fromValues(KillType::values);

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
