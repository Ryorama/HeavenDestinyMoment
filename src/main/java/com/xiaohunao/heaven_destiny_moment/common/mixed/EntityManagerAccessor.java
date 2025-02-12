package com.xiaohunao.heaven_destiny_moment.common.mixed;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

public interface EntityManagerAccessor {
    PersistentEntitySectionManager<Entity> getEntityManager();
}

