package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.xiaohunao.heaven_destiny_moment.common.context.*;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnCategoryMultiplierInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorMixin {
    @Shadow
    @Final
    public ChunkMap chunkMap;

    @Shadow @Final private Long2ObjectMap<List<ServerPlayer>> playersNearChunk;
    @Shadow @Final private Map<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts;

    @Inject(method = "canSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LocalMobCapCalculator$MobCounts;canSpawn(Lnet/minecraft/world/entity/MobCategory;)Z"), cancellable = true)
    private void canSpawn(MobCategory category, ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        List<ServerPlayer> serverPlayers = this.playersNearChunk.computeIfAbsent(pos.toLong(), (p_186511_) -> this.chunkMap.getPlayersCloseForSpawning(pos));
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(chunkMap.level);

        for(ServerPlayer serverplayer : serverPlayers) {
            LocalMobCapCalculator.MobCounts localmobcapcalculator$mobcounts = this.playerMobCounts.get(serverplayer);
            if (localmobcapcalculator$mobcounts == null){
                return;
            }

            for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
                Boolean aBoolean = Optional.of(instance.getMoment())
                        .filter(moment -> moment.isInArea((ServerLevel) serverplayer.level(), serverplayer.blockPosition()))
                        .flatMap(Moment::momentData)
                        .flatMap(MomentData::entitySpawnSettings)
                        .flatMap(EntitySpawnSettings::biomeEntitySpawnSettings)
                        .flatMap(BiomeEntitySpawnSettings::spawnCategoryMultiplier)
                        .map(multiplierMap -> {
                            Object2IntMap<MobCategory> counts = localmobcapcalculator$mobcounts.counts;
                            final int currentCount = counts.getOrDefault(category, 0);

                            SpawnCategoryMultiplierInstanceMixed spawnCategoryMultiplierInstanceMixed = (SpawnCategoryMultiplierInstanceMixed) chunkMap.level;
                            SpawnCategoryMultiplierInstance multiplierInstance = spawnCategoryMultiplierInstanceMixed.hdm$getMobCategoryMultiplierInstance(category);
                            SpawnCategoryMultiplierModifier multiplierModifier = multiplierMap.get(category);
                            if (multiplierModifier != null){
                                multiplierInstance.addModifier(multiplierModifier);
                                double maxLimit = category.getMaxInstancesPerChunk() * multiplierInstance.getValue();
                                return currentCount < maxLimit;
                            }
                            return currentCount < category.getMaxInstancesPerChunk();
                        })
                        .orElse(false);
                cir.setReturnValue(aBoolean);
            }
        }
    }
}
