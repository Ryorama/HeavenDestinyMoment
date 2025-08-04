package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnerDataMomentMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MobSpawnSettings.SpawnerData.class)
public class SpawnerDataMixin implements SpawnerDataMomentMixed {

    @Unique
    private Moment heaven_destiny_moment$moment;

    @Override
    @Unique
    public Moment heaven_destiny_moment$getMoment() {
        return heaven_destiny_moment$moment;
    }

    @Override
    @Unique
    public MobSpawnSettings.SpawnerData heaven_destiny_moment$setMoment(Moment heaven_destiny_moment$moment) {
        this.heaven_destiny_moment$moment = heaven_destiny_moment$moment;
        return (MobSpawnSettings.SpawnerData) (Object) this;
    }

    @Override
    public MobSpawnSettings.SpawnerData heaven_destiny_moment$vanillaSource() {
        this.heaven_destiny_moment$moment = HeavenDestinyMoment.EMITY_MOMENT;
        return (MobSpawnSettings.SpawnerData) (Object) this;
    }
}
