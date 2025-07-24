package com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.Vec3;

public class RandomPlayerPosImitationVanillaNaturalSpawner implements ISpawnAlgorithm{
    public static final RandomPlayerPosImitationVanillaNaturalSpawner INSTANCE = new RandomPlayerPosImitationVanillaNaturalSpawner();
    public static final MapCodec<RandomPlayerPosImitationVanillaNaturalSpawner> CODEC = MapCodec.unit(() -> INSTANCE);


    @Override
    public Vec3 spawn(MomentInstance momentInstance, Entity entity) {
        Level level = momentInstance.getLevel();
        Player randomPlayer = momentInstance.getPlayerListManager().getRandomPlayer();
        if(randomPlayer != null){
            return Vec3.atLowerCornerOf(NaturalSpawner.getRandomPosWithin(level, level.getChunkAt(randomPlayer.blockPosition())));
        }
        return momentInstance.getRandomSpawnPos() == null ? Vec3.ZERO : momentInstance.getRandomSpawnPos();
    }

    @Override
    public MapCodec<? extends ISpawnAlgorithm> codec() {
        return HDMContextRegister.RANDOM_PLAYER_POS_IMITATION_VANILLA_NATURAL_SPAWNER.get();
    }
}
