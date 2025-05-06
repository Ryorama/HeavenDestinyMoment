package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.sugar.Local;
import com.xiaohunao.heaven_destiny_moment.common.context.BiomeEntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.EntitySpawnSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MobSpawnRule;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    @Inject(method = "mobsAt", at = @At("RETURN"), cancellable = true)
    private static void mobsAt(ServerLevel serverLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, BlockPos pos, Holder<Biome> biomeHolder, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
                    .filter(moment -> moment.isInArea(serverLevel, pos))
                    .flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .ifPresent(entitySpawnSettingsContext -> {
                        List<MobSpawnSettings.SpawnerData> unwrap = new ArrayList<>(cir.getReturnValue().unwrap());
                        cir.setReturnValue(entitySpawnSettingsContext.adjustmentBiomeEntitySpawnSettings(mobCategory, unwrap));
                    });
        }
    }


    @Inject(method = "getRoughBiome", at = @At("RETURN"), cancellable = true)
    private static void getRoughBiome(BlockPos pos, ChunkAccess chunk, CallbackInfoReturnable<Biome> cir) {
        Level level = ((LevelChunk) chunk).getLevel();
        if (level.isClientSide) {
            return;
        }
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
        Biome.BiomeBuilder fakeBiome = new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .skyColor(1)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                );

        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
                    .filter(moment -> moment.isInArea((ServerLevel) level, pos))
                    .flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .ifPresent(entitySpawnSettingsContext -> {
                        MobSpawnSettings mobSettings = cir.getReturnValue().getMobSettings();


                        entitySpawnSettingsContext.biomeEntitySpawnSettings().flatMap(BiomeEntitySpawnSettings::biomeMobSpawnSettings).ifPresent(mobSpawnSettings -> {
                            Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners = Maps.newHashMap(mobSettings.spawners);
                            Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> newSpawners = Maps.newHashMap();
                            for (Map.Entry<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> entry : spawners.entrySet()) {
                                MobCategory mobCategory = entry.getKey();
                                WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = entry.getValue();
                                List<MobSpawnSettings.SpawnerData> unwrap = Lists.newArrayList(weightedRandomList.unwrap());
                                newSpawners.put(mobCategory, entitySpawnSettingsContext.adjustmentBiomeEntitySpawnSettings(mobCategory, unwrap));
                            }

                            Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.newHashMap(mobSettings.mobSpawnCosts);
                            for (Map.Entry<EntityType<?>, MobSpawnSettings.MobSpawnCost> entry : mobSpawnCosts.entrySet()) {
                                EntityType<?> entityType = entry.getKey();
                                MobSpawnSettings.MobSpawnCost mobSpawnCost = entry.getValue();
                                MobSpawnSettings.MobSpawnCost newCost = mobSpawnSettings.mobSpawnCosts.get(entityType);
//                                if (entitySpawnSettingsContext.rule().flatMap(MobSpawnRule::allowOriginalBiomeSpawnSettings).orElse(true)) {
//                                    mobSpawnCosts.put(entityType, mobSpawnCost);
//                                    if (newCost != null) {
//                                        mobSpawnCosts.put(entityType, newCost);
//                                    }
//                                }else {
//                                    if (newCost == null) {
//                                        mobSpawnCosts.remove(entityType);
//                                    }
//                                }
                            }


                            float oldCreatureProbability = mobSettings.getCreatureProbability();
                            float newCreatureProbability = mobSpawnSettings.getCreatureProbability();

                            fakeBiome.mobSpawnSettings(new MobSpawnSettings(Math.max(oldCreatureProbability, newCreatureProbability), newSpawners, mobSpawnCosts));
                        });

                        if (fakeBiome.mobSpawnSettings == null) {
                            fakeBiome.mobSpawnSettings(mobSettings);
                        }


                        fakeBiome.generationSettings(BiomeGenerationSettings.EMPTY);
                        cir.setReturnValue(fakeBiome.build());
                    });

        }
    }

    @Inject(method = "isRightDistanceToPlayerAndSpawnPoint", at = @At("RETURN"), cancellable = true)
    private static void isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double distance, CallbackInfoReturnable<Boolean> cir) {
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel);
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            Optional.of(instance.getMoment())
                    .filter(moment -> moment.isInArea(serverLevel, pos))
                    .flatMap(Moment::momentData)
                    .flatMap(MomentData::entitySpawnSettings)
                    .flatMap(EntitySpawnSettings::rule)
                    .flatMap(MobSpawnRule::ignoreDistance)
                    .ifPresent(cir::setReturnValue);
        }
    }

//    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
//    private static void isValidSpawnPostitionForType(ServerLevel level, MobCategory category, StructureManager structureManager, ChunkGenerator generator, MobSpawnSettings.SpawnerData data, BlockPos.MutableBlockPos pos, double distance, CallbackInfoReturnable<Boolean> cir){
//        EntityType<?> entitytype = data.type;
//        boolean spawnPositionOk = SpawnPlacements.isSpawnPositionOk(entitytype, level, pos);
//        System.out.println("isValidSpawnPostitionForType :" + spawnPositionOk);
//    }


//    @Inject(method = "getRandomPosWithin", at = @At("RETURN"), cancellable = true)
//    private static void forceSurface(Level level, LevelChunk levelChunk, CallbackInfoReturnable<BlockPos> cir) {
//        if (level.isClientSide) {
//            return;
//        }
//
//        BlockPos returnValue = cir.getReturnValue();
//        MomentManager momentManager = MomentManager.of(level);
//        for (MomentInstance instance : momentManager.getRunMoments().values()) {
//            instance.getMoment()
//                    .filter(moment -> moment.isInArea((ServerLevel) level, returnValue))
//                    .map(Moment::getMomentDataContext)
//                    .flatMap(MomentDataContext::entitySpawnSettings)
//                    .flatMap(EntitySpawnSettingsContext::rule)
//                    .flatMap(MobSpawnRule::forceSurfaceSpawning)
//                    .ifPresent(mobSpawnSettingsContext -> {
//                        Player closestPlayer = level.getNearestPlayer(returnValue.getX(), returnValue.getY(), returnValue.getZ(), -1.0, false);
//                        if (closestPlayer != null) {
//                            BlockPos closestPlayerPosition = closestPlayer.blockPosition();
//                            if (closestPlayerPosition.getY() > level.getHeight(Heightmap.Types.WORLD_SURFACE, closestPlayerPosition.getX(), closestPlayerPosition.getZ())) {
//                                cir.setReturnValue(new BlockPos(returnValue.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE, returnValue.getX(), returnValue.getZ()) + 1, returnValue.getZ()));
//                            }
//                        }
//                    });
//        }
//    }

//    @ModifyReceiver(method = "spawnMobsForChunkGeneration", at = @At(value = "INVOKE",
//            target = "Lnet/minecraft/world/level/ServerLevelAccessor;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
//    private static ServerLevelAccessor spawnMobsForChunkGeneration(ServerLevelAccessor serverLevelAccessor, Entity entity) {
//        MomentManager momentManager = MomentManager.of(serverLevelAccessor.getLevel());
//        for (MomentInstance instance : momentManager.getRunMoments().values()) {
//            instance.getMoment()
//                    .filter(moment -> moment.isInArea(serverLevelAccessor.getLevel(), entity.blockPosition()))
//                    .ifPresent(moment -> {
//                        instance.finalizeSpawn(entity);
//                    });
//
//        }
//        return serverLevelAccessor;
//    }

//    @Inject(method = "getRandomSpawnMobAt", at = @At("RETURN"), cancellable = true)
//    private static void getRandomSpawnMobAt(ServerLevel level, StructureManager structureManager, ChunkGenerator generator, MobCategory category, RandomSource random, BlockPos pos, CallbackInfoReturnable<Optional<MobSpawnSettings.SpawnerData>> cir) {
//        Optional<MobSpawnSettings.SpawnerData> returnValue = cir.getReturnValue();
//        if (returnValue.isPresent()){
//            System.out.println(returnValue);
//        }
//    }


    @Inject(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    private static void spawnCategoryForPosition(MobCategory category, ServerLevel serverLevel, ChunkAccess chunk, BlockPos pos, NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci, @Local Mob mob, @Local MobSpawnSettings.SpawnerData spawnerData) {
        if (spawnerData instanceof BiomeEntitySpawnSettings.OwnSpawnerData) {
            MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(serverLevel.getLevel());
            for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
                if (instance.canSpawnEntity(serverLevel, mob, pos)) {
                    instance.addEnemy(mob);
                } else {
                    ci.cancel();
                }
            }
        }
    }
}



//    @Inject(method = "spawnCategoryForChunk" ,at = @At(value = "HEAD"), cancellable = true)
//    private static void spawnCategoryForChunk(MobCategory category, ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnPredicate filter, NaturalSpawner.AfterSpawnCallback callback, CallbackInfo ci){
//        MomentManager momentManager = MomentManager.of(level);
//        for (MomentInstance<?> instance : momentManager.getImmutableRunMoments().values()) {
//
//        }
//        NaturalSpawner.spawnCategoryForPosition(category, level, chunk, serverPlayer.blockPosition(), filter, callback);
//        ci.cancel();
//
//}
