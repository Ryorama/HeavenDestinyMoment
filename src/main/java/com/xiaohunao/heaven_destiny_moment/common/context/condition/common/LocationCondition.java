package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.IBuilderConverter;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public record LocationCondition(Optional<LocationPredicate.PositionPredicate> position, Optional<List<ResourceKey<Biome>>> biomes,
                                Optional<List<ResourceKey<Structure>>> structures, Optional<List<ResourceKey<Level>>> dimension,
                                Optional<Boolean> smokey, Optional<LightPredicate> light, Optional<BlockPredicate> block,
                                Optional<FluidPredicate> fluid, Optional<Boolean> canSeeSky) implements ICondition {
    private static final Logger log = LoggerFactory.getLogger(LocationCondition.class);
    public static final ResourceLocation ID = HeavenDestinyMoment.asResource("location");
    public static final MapCodec<LocationCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LocationPredicate.PositionPredicate.CODEC.optionalFieldOf("position").forGetter(LocationCondition::position),
            ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("biomes").forGetter(LocationCondition::biomes),
            ResourceKey.codec(Registries.STRUCTURE).listOf().optionalFieldOf("structures").forGetter(LocationCondition::structures),
            ResourceKey.codec(Registries.DIMENSION).listOf().optionalFieldOf("dimension").forGetter(LocationCondition::dimension),
            Codec.BOOL.optionalFieldOf("smokey").forGetter(LocationCondition::smokey),
            LightPredicate.CODEC.optionalFieldOf("light").forGetter(LocationCondition::light),
            BlockPredicate.CODEC.optionalFieldOf("block").forGetter(LocationCondition::block),
            FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(LocationCondition::fluid),
            Codec.BOOL.optionalFieldOf("canSeeSky").forGetter(LocationCondition::canSeeSky)
    ).apply(instance, LocationCondition::new));

    @Override
    public boolean matches(AutomationContext context) {
        if (context.blockPos().isPresent()) {
            Level level = context.getLevel();
            BlockPos blockPos = context.blockPos().get();

            if (level instanceof ServerLevel serverLevel) {
                return matches(serverLevel, blockPos);
            }
        }

        return false;
    }

    public boolean matches(ServerLevel level, BlockPos blockPos) {
        return checkCondition(() -> isBlockLoaded(level, blockPos), "Pos not loaded", blockPos) &&
                checkCondition(() -> matchesPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ()), "Pos does not match", blockPos) &&
                checkCondition(() -> matchesDimension(level), "Dimension does not match", level.dimension().location()) &&
                checkCondition(() -> matchesBiome(level, blockPos), "Biome does not match", blockPos) &&
                checkCondition(() -> matchesStructure(level, blockPos), "Structure does not match", blockPos) &&
                checkCondition(() -> matchesSmokey(level, blockPos), "Smokey condition does not match", blockPos) &&
                checkCondition(() -> matchesLight(level, blockPos), "Light level does not match", blockPos) &&
                checkCondition(() -> matchesBlock(level, blockPos), "Block does not match", blockPos) &&
                checkCondition(() -> matchesFluid(level, blockPos), "Fluid does not match", blockPos) &&
                checkCondition(() -> matchesCanSeeSky(level, blockPos), "CanSeeSky condition does not match", blockPos);
    }

    private boolean checkCondition(BooleanSupplier condition, String failureMessage, Object detail) {
        if (!condition.getAsBoolean()) {
//            log.warn("{} at {}", failureMessage, detail);
            return false;
        }
        return true;
    }


    private boolean isBlockLoaded(ServerLevel level,BlockPos pos) {
//        return level.isLoaded(pos);
        return true;
    }

    private boolean matchesPosition(double x, double y, double z) {
        return position.map(p -> p.matches(x, y, z)).orElse(true);
    }

    private boolean matchesDimension(ServerLevel level) {
        return dimension.map(d -> d.contains(level.dimension())).orElse(true);
    }

    private boolean matchesBiome(ServerLevel level, BlockPos pos) {
        return biomes.map(b -> b.contains(level.getBiome(pos).getKey())).orElse(true);
    }

    private boolean matchesStructure(ServerLevel level, BlockPos pos) {
        return structures.map(s -> level.structureManager().getStructureWithPieceAt(pos, holder -> holder.is(s::contains)).isValid()).orElse(true);
    }

    private boolean matchesSmokey(ServerLevel level, BlockPos pos) {
        return smokey.map(s -> s == CampfireBlock.isSmokeyPos(level, pos)).orElse(true);
    }

    private boolean matchesLight(ServerLevel level, BlockPos pos) {
        return light.map(l -> l.matches(level, pos)).orElse(true);
    }

    private boolean matchesBlock(ServerLevel level, BlockPos pos) {
        return block.map(b -> b.matches(level, pos)).orElse(true);
    }

    private boolean matchesFluid(ServerLevel level, BlockPos pos) {
        return fluid.map(f -> f.matches(level, pos)).orElse(true);
    }

    private boolean matchesCanSeeSky(ServerLevel level, BlockPos pos) {
        return canSeeSky.map(s -> s == level.canSeeSky(pos)).orElse(true);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public static class Builder implements IBuilderConverter<LocationCondition> {
        private MinMaxBounds.Doubles x;
        private MinMaxBounds.Doubles y;
        private MinMaxBounds.Doubles z;
        private Optional<List<ResourceKey<Biome>>> biomes;
        private Optional<List<ResourceKey<Structure>>> structures;
        private Optional<List<ResourceKey<Level>>> dimension;
        private Optional<Boolean> smokey;
        private Optional<LightPredicate> light;
        private Optional<BlockPredicate> block;
        private Optional<FluidPredicate> fluid;
        private Optional<Boolean> canSeeSky;


        public Builder() {
            this.x = MinMaxBounds.Doubles.ANY;
            this.y = MinMaxBounds.Doubles.ANY;
            this.z = MinMaxBounds.Doubles.ANY;
            this.biomes = Optional.empty();
            this.structures = Optional.empty();
            this.dimension = Optional.empty();
            this.smokey = Optional.empty();
            this.light = Optional.empty();
            this.block = Optional.empty();
            this.fluid = Optional.empty();
            this.canSeeSky = Optional.empty();
        }

        public static Builder location() {
            return new Builder();
        }

        public LocationCondition build() {
            Optional<LocationPredicate.PositionPredicate> optional = LocationPredicate.PositionPredicate.of(this.x, this.y, this.z);
            return new LocationCondition(optional, this.biomes, this.structures, this.dimension, this.smokey, this.light, this.block, this.fluid, this.canSeeSky);
        }

        @SafeVarargs
        public static Builder inStructure(ResourceKey<Structure>... structure) {
            return location().setStructures(List.of(structure));
        }

        @SafeVarargs
        public static Builder inBiome(ResourceKey<Biome>... biome) {
            return location().setBiomes(List.of(biome));
        }

        @SafeVarargs
        public static Builder inDimension(ResourceKey<Level>... dimension) {
            return location().setDimension(List.of(dimension));
        }

        public static Builder isCanSeeSky(boolean isCanSeeSky) {
            return location().setCanSeeSky(isCanSeeSky);
        }

        public static Builder atYLocation(MinMaxBounds.Doubles y) {
            return location().setY(y);
        }

        public Builder setX(MinMaxBounds.Doubles x) {
            this.x = x;
            return this;
        }

        public Builder setY(MinMaxBounds.Doubles y) {
            this.y = y;
            return this;
        }

        public Builder setZ(MinMaxBounds.Doubles z) {
            this.z = z;
            return this;
        }

        public Builder setStructures(List<ResourceKey<Structure>> structures) {
            this.structures = Optional.of(structures);
            return this;
        }

        @SafeVarargs
        public final Builder setStructures(ResourceKey<Structure>... structures) {
            this.structures = Optional.of(List.of(structures));
            return this;
        }

        public Builder setBiomes(List<ResourceKey<Biome>> biomes) {
            this.biomes = Optional.of(biomes);
            return this;
        }

        @SafeVarargs
        public final Builder setBiomes(ResourceKey<Biome>... biomes) {
            this.biomes = Optional.of(List.of(biomes));
            return this;
        }

        public Builder setDimension(List<ResourceKey<Level>> dimension) {
            this.dimension = Optional.of(dimension);
            return this;
        }

        @SafeVarargs
        public final Builder setDimension(ResourceKey<Level>... dimension) {
            this.dimension = Optional.of(List.of(dimension));
            return this;
        }

        public Builder setLight(LightPredicate.Builder light) {
            this.light = Optional.of(light.build());
            return this;
        }

        public Builder setBlock(BlockPredicate.Builder block) {
            this.block = Optional.of(block.build());
            return this;
        }

        public Builder setFluid(FluidPredicate.Builder fluid) {
            this.fluid = Optional.of(fluid.build());
            return this;
        }

        public Builder setSmokey(boolean smokey) {
            this.smokey = Optional.of(smokey);
            return this;
        }

        public Builder setCanSeeSky(boolean canSeeSky) {
            this.canSeeSky = Optional.of(canSeeSky);
            return this;
        }

        @Override
        public Builder converter(LocationCondition locationCondition) {
            Builder builder = new Builder();
            locationCondition.position().ifPresent(pos -> {
                builder.x = pos.x();
                builder.y = pos.y();
                builder.z = pos.z();
            });
            locationCondition.biomes().ifPresent(biomes -> builder.biomes = Optional.of(new ArrayList<>(biomes)));
            locationCondition.structures().ifPresent(structures -> builder.structures = Optional.of(new ArrayList<>(structures)));
            locationCondition.dimension().ifPresent(dimension -> builder.dimension = Optional.of(new ArrayList<>(dimension)));
            locationCondition.smokey().ifPresent(smokey -> builder.smokey = Optional.of(smokey));
            locationCondition.light().ifPresent(light -> builder.light = Optional.of(light));
            locationCondition.block().ifPresent(block -> builder.block = Optional.of(block));
            locationCondition.fluid().ifPresent(fluid -> builder.fluid = Optional.of(fluid));
            locationCondition.canSeeSky().ifPresent(canSeeSky -> builder.canSeeSky = Optional.of(canSeeSky));
            return builder;
        }
    }

}
