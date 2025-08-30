package com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class RandomPlayerPosImitationVanillaNaturalSpawner implements ISpawnAlgorithm{
    public static final RandomPlayerPosImitationVanillaNaturalSpawner INSTANCE = new RandomPlayerPosImitationVanillaNaturalSpawner();
    public static final MapCodec<RandomPlayerPosImitationVanillaNaturalSpawner> CODEC = MapCodec.unit(() -> INSTANCE);


    @Override
    public Vec3 spawn(MomentInstance momentInstance, Entity entity) {
        Level level = momentInstance.getLevel();
        Player randomPlayer = momentInstance.getPlayerListManager().getRandomPlayer();
        if(randomPlayer != null){
            Vec3 vec3 = Vec3.atLowerCornerOf(getRandomPosWithin(level, randomPlayer));
            boolean loaded = level.isLoaded(new BlockPos((int) vec3.x, (int) vec3.y, (int) vec3.z));
            return loaded ? vec3 : getRandomSpawnPos(level,randomPlayer).getCenter();
        }
        return momentInstance.getRandomSpawnPos() == null ? Vec3.ZERO : momentInstance.getRandomSpawnPos();
    }

    @Override
    public MapCodec<? extends ISpawnAlgorithm> codec() {
        return CODEC;
    }

    public BlockPos getRandomSpawnPos(Level level, Player player) {
        if (player == null || !level.isLoaded(player.blockPosition())) {
            return BlockPos.ZERO;
        }
        int x = player.getBlockX() + level.random.nextInt(32) - 16;
        int y = player.getBlockY() + level.random.nextInt(16) - 8;
        int z = player.getBlockZ() + level.random.nextInt(32) - 16;
        return new BlockPos(x, y, z);
    }

    public static BlockPos getRandomPosWithin(Level level, Player player) {
        // 获取玩家所在的区块位置
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());
        // 在玩家所在区块的基础上，随机偏移一定范围内的区块（例如：偏移量为1，即相邻的区块）
        int offsetX = level.random.nextInt(3) - 1; // -1, 0, 或 1
        int offsetZ = level.random.nextInt(3) - 1; // -1, 0, 或 1
        ChunkPos targetChunkPos = new ChunkPos(playerChunkPos.x + offsetX, playerChunkPos.z + offsetZ);

        // 在目标区块内随机选择一个位置
        int x = targetChunkPos.getMinBlockX() + level.random.nextInt(16);
        int z = targetChunkPos.getMinBlockZ() + level.random.nextInt(16);
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z); // 直接获取地表高度

        return new BlockPos(x, y, z);
    }
}
