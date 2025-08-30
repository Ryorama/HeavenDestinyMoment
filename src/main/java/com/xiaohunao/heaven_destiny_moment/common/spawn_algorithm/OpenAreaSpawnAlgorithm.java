package com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record OpenAreaSpawnAlgorithm(int maxTry, int range, int verticalSearchRange, Optional<Direction> dir) implements ISpawnAlgorithm {
    public static final OpenAreaSpawnAlgorithm DEFAULT = new OpenAreaSpawnAlgorithm(16, 32, 5,Optional.empty());

    public static final MapCodec<OpenAreaSpawnAlgorithm> CODEC =  RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("maxTry",16).forGetter(OpenAreaSpawnAlgorithm::maxTry),
            Codec.INT.optionalFieldOf("range",32).forGetter(OpenAreaSpawnAlgorithm::range),
            Codec.INT.optionalFieldOf("verticalSearchRange",5).forGetter(OpenAreaSpawnAlgorithm::verticalSearchRange),
            Direction.CODEC.optionalFieldOf("dir").forGetter(OpenAreaSpawnAlgorithm::dir)
    ).apply(instance, OpenAreaSpawnAlgorithm::new));


    @Override
    public Vec3 spawn(MomentInstance momentInstance, Entity entity) {
        Level level = momentInstance.getLevel();
        Vec3 pos = momentInstance.getRandomSpawnPos();
        AABB entityBoundingBox = entity.getBoundingBox();
        double entityWidth = entityBoundingBox.getXsize();
        double entityHeight = entityBoundingBox.getYsize();

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        int verticalSearchRange = 5; // 向上和向下最多搜索5个方块

        for(int i = 0; i < maxTry; ++i) {
            Vec3i directionVector = dir.map(Direction::getNormal)
                    .map(vec3i -> {
                        double dx = vec3i.getX() * level.getRandom().nextInt(range);
                        double dz = vec3i.getZ() * level.getRandom().nextInt(range);
                        return new Vec3i((int) dx, 0, (int) dz);
                    })
                    .orElseGet(() -> {
                        float angle = level.random.nextFloat() * (float) Math.PI * 2F;
                        double dx = Math.cos(angle) * level.getRandom().nextInt(range);
                        double dz = Math.sin(angle) * level.getRandom().nextInt(range);
                        return new Vec3i((int) dx, 0, (int) dz);
                    });

            double x = pos.x() + directionVector.getX() + level.random.nextInt(5);
            double z = pos.z() + directionVector.getZ() + level.random.nextInt(5);

            int baseY = (int)pos.y();
            int minSearchY = Math.max(level.getMinBuildHeight(), baseY - verticalSearchRange);
            int maxSearchY = Math.min(level.getMaxBuildHeight(), baseY + verticalSearchRange);

            for (int y = maxSearchY; y >= minSearchY; y--) {
                mutableBlockPos.set((int)x, y, (int)z);

                if (!isSafeGround(level, mutableBlockPos)) {
                    continue;
                }

                if (!isChunkLoaded(level, x, z)) {
                    continue;
                }

                AABB potentialSpawnBox = new AABB(
                        x - entityWidth/2, y, z - entityWidth/2,
                        x + entityWidth/2, y + entityHeight, z + entityWidth/2
                );

                if (!hasCollisions(level, potentialSpawnBox)) {
                    // 找到安全位置，返回坐标（x坐标加0.5使实体生成在方块中心）
                    return new Vec3(x + 0.5D, y, z + 0.5D);
                }
            }
        }

        return Vec3.ZERO;
    }

    private boolean isChunkLoaded(Level level, double x, double z) {
        int chunkX = (int)Math.floor(x) >> 4;
        int chunkZ = (int)Math.floor(z) >> 4;
        return level.hasChunk(chunkX, chunkZ);
    }


    private boolean isSafeGround(Level level, BlockPos pos) {
        BlockState groundState = level.getBlockState(pos.below());
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        return groundState.isFaceSturdy(level, pos.below(), Direction.UP)
                && feetState.getCollisionShape(level, pos).isEmpty()
                && headState.getCollisionShape(level, pos.above()).isEmpty();
    }


    private boolean hasCollisions(Level level, AABB box) {
        return !level.noCollision(box);
    }


    @Override
    public MapCodec<? extends ISpawnAlgorithm> codec() {
        return CODEC;
    }

    public static class Builder {
        private int maxTry = 16;
        private int range = 32;
        private int verticalSearchRange = 5;
        private Direction dir;

        public OpenAreaSpawnAlgorithm build() {
            return new OpenAreaSpawnAlgorithm(maxTry, range, verticalSearchRange, Optional.ofNullable(dir));
        }

        public Builder maxTry(int maxTry) {
            this.maxTry = maxTry;
            return this;
        }

        public Builder range(int range,int verticalSearchRange) {
            this.range = range;
            this.verticalSearchRange = verticalSearchRange;
            return this;
        }

        public Builder direction(Direction dir) {
            this.dir = dir;
            return this;
        }
    }
}
