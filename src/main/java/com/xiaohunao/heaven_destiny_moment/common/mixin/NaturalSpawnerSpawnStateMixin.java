package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.xiaohunao.heaven_destiny_moment.common.context.*;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnCategoryMultiplierInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(NaturalSpawner.SpawnState.class)
public class NaturalSpawnerSpawnStateMixin {
    @Shadow
    @Final
    private LocalMobCapCalculator localMobCapCalculator;

    @Shadow
    @Final
    private int spawnableChunkCount;

    @Shadow
    @Final
    private Object2IntOpenHashMap<MobCategory> mobCategoryCounts;

    @Inject(method = "canSpawnForCategory", at = @At("HEAD"), cancellable = true)
    private void modifySpawnCapByCategory(MobCategory mobCategory, ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = localMobCapCalculator.chunkMap.level;
        int maxInstancesPerChunk = mobCategory.getMaxInstancesPerChunk();
        int currentCount = this.mobCategoryCounts.getInt(mobCategory);
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
                    .flatMap(IMoment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .flatMap(EntitySpawnSettings::biomeEntitySpawnSettings)
                    .flatMap(BiomeEntitySpawnSettings::spawnCategoryMultiplier)
                    .ifPresent(multiplierMap -> {
                        SpawnCategoryMultiplierInstanceMixed spawnCategoryMultiplierInstanceMixed = (SpawnCategoryMultiplierInstanceMixed) level;
                        SpawnCategoryMultiplierInstance multiplierInstance = spawnCategoryMultiplierInstanceMixed.hdm$getMobCategoryMultiplierInstance(mobCategory);

                        SpawnCategoryMultiplierModifier multiplierModifier = multiplierMap.get(mobCategory);
                        if (multiplierModifier != null){
                            double spawnMultiplier = multiplierInstance.getValue();
                            int maxLimit = (int) (maxInstancesPerChunk * (this.spawnableChunkCount * spawnMultiplier) / NaturalSpawner.MAGIC_NUMBER);
                            if (currentCount >= maxLimit) {
                                cir.setReturnValue(false);
                            } else {
                                cir.setReturnValue(this.localMobCapCalculator.canSpawn(mobCategory, chunkPos));
                            }
                        }
                    });
        }
    }
}
