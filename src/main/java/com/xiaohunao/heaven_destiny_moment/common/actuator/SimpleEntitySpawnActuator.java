package com.xiaohunao.heaven_destiny_moment.common.actuator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMActuators;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.List;

public record SimpleEntitySpawnActuator(IEntityInfo entityInfo, ISpawnAlgorithm spawnAlgorithm) implements IActuator {
    public static final MapCodec<SimpleEntitySpawnActuator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IEntityInfo.CODEC.fieldOf("entity_info").forGetter(SimpleEntitySpawnActuator::entityInfo),
            ISpawnAlgorithm.CODEC.fieldOf("spawn_algorithm").forGetter(SimpleEntitySpawnActuator::spawnAlgorithm)
    ).apply(instance, SimpleEntitySpawnActuator::new));

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
        return HDMActuators.SIMPLE_ENTITY_SPAWN_ACTUATOR.get();
    }
}
