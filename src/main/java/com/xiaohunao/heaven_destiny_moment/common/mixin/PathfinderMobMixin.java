package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MobSpawnRule;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PathfinderMob.class)
public class PathfinderMobMixin {
    @Inject(method = "checkSpawnRules", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void checkSpawnRules(LevelAccessor pLevel, MobSpawnType pSpawnReason, CallbackInfoReturnable<Boolean> cir) {
        if (!(pLevel instanceof ServerLevel serverLevel)) return;

        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);
        PathfinderMob mob = (PathfinderMob) (Object) this;
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
                    .filter(moment -> moment.isInArea(serverLevel, mob.blockPosition()))
                    .flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .flatMap(EntitySpawnSettings::rule)
                    .flatMap(MobSpawnRule::ignoreLightLevel)
                    .ifPresent(cir::setReturnValue);
        }
    }
}