package com.xiaohunao.heaven_destiny_moment.common.mixed;

import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import net.minecraft.world.level.biome.MobSpawnSettings;

public interface SpawnerDataMomentMixed {
    IMoment heaven_destiny_moment$getMoment();

    MobSpawnSettings.SpawnerData heaven_destiny_moment$setMoment(IMoment heaven_destiny_moment$moment);

    MobSpawnSettings.SpawnerData heaven_destiny_moment$vanillaSource();
}
