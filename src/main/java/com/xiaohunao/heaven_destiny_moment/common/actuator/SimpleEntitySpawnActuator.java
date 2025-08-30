package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public final class SimpleEntitySpawnActuator implements IActuator {
    public static final MapCodec<SimpleEntitySpawnActuator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IEntityInfo.CODEC.fieldOf("entity_info").forGetter(SimpleEntitySpawnActuator::entityInfo),
            ISpawnAlgorithm.CODEC.fieldOf("spawn_algorithm").forGetter(SimpleEntitySpawnActuator::spawnAlgorithm)
    ).apply(instance, SimpleEntitySpawnActuator::new));
    private final IEntityInfo entityInfo;
    private final ISpawnAlgorithm spawnAlgorithm;

    public SimpleEntitySpawnActuator(IEntityInfo entityInfo, ISpawnAlgorithm spawnAlgorithm) {
        this.entityInfo = entityInfo;
        this.spawnAlgorithm = spawnAlgorithm;
    }

    public static SimpleEntitySpawnActuator of(IEntityInfo entityInfo, ISpawnAlgorithm spawnAlgorithm) {
        return new SimpleEntitySpawnActuator(entityInfo, spawnAlgorithm);
    }

    public void execute(MomentInstance momentInstance) {
        Level level = momentInstance.getLevel();
        List<Entity> spawn = entityInfo.spawn(level);
        for (Entity entity : spawn) {
            entity.setPos(spawnAlgorithm.spawn(momentInstance, entity));
            level.addFreshEntity(entity);
            momentInstance.addEnemy(entity);
        }
    }

    @Override
    public MapCodec<? extends IActuator> codec() {
        return CODEC;
    }

    public IEntityInfo entityInfo() {
        return entityInfo;
    }

    public ISpawnAlgorithm spawnAlgorithm() {
        return spawnAlgorithm;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleEntitySpawnActuator) obj;
        return Objects.equals(this.entityInfo, that.entityInfo) &&
                Objects.equals(this.spawnAlgorithm, that.spawnAlgorithm);
    }

}
