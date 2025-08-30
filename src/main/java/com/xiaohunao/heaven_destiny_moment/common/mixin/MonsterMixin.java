package com.xiaohunao.heaven_destiny_moment.common.mixin;


import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MobSpawnRule;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Monster.class)
public class MonsterMixin {
    @Inject(method = "isDarkEnoughToSpawn", at = @At("RETURN"), cancellable = true)
    private static void isDarkEnoughToSpawn(ServerLevelAccessor serverLevelAccessor, BlockPos pos, RandomSource p_219012_, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = serverLevelAccessor.getLevel();
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
//                    .filter(moment -> moment.isInArea(level,pos))
                    .flatMap(IMoment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .flatMap(EntitySpawnSettings::rule)
                    .flatMap(MobSpawnRule::ignoreLightLevel)
                    .ifPresent(cir::setReturnValue);
        }
    }
}
