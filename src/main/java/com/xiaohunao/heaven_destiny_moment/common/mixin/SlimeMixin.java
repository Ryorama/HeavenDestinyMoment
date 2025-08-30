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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static net.minecraft.world.entity.Mob.checkMobSpawnRules;

@Mixin(Slime.class)
public class SlimeMixin {

    @Inject(method = "checkSlimeSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void checkSlimeSpawnRules(EntityType<Slime> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof ServerLevel serverLevel) {
            MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);
            for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
                Optional.of(instance.getMoment())
//                        .filter(moment -> moment.isInArea(serverLevel, pos))
                        .flatMap(IMoment::momentData)
                        .flatMap(MomentData::entitySpawnSettings)
                        .flatMap(EntitySpawnSettings::rule)
                        .flatMap(MobSpawnRule::slimesSpawnEverywhere)
                        .ifPresent(slimesSpawnEverywhere -> {
                            boolean origin = pos.getY() > 50 &&
//                                    pos.getY() < 70 &&
                                    random.nextFloat() < 0.5F &&
                                    random.nextFloat() < level.getMoonBrightness() &&
                                    level.getMaxLocalRawBrightness(pos) <= random.nextInt(8);

                            if (slimesSpawnEverywhere && origin && pos.getY() >= serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1) {
                                cir.setReturnValue(checkMobSpawnRules(entityType, serverLevel, spawnType, pos, random));
                            }
                        });
            }
        }
    }
}