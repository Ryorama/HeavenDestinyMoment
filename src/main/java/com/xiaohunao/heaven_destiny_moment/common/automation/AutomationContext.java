package com.xiaohunao.heaven_destiny_moment.common.automation;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public class AutomationContext {
    //规范化的上下文的key名
    private static final String KEY_MOMENT_INSTANCE_UUID = "moment_instance_uuid";
    private static final String KEY_BLOCK = "block";
    private static final String KEY_ENTITY_TYPE = "entity_type";
    private static final String KEY_POS = "pos";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_CURRENT_GAME_TIME = "current_game_time";
    private static final String KEY_CURRENT_DAY_TIME = "current_day_time";

    private final Level level;
    private final Player player;
    private final MomentInstance momentInstance;
    private final CompoundTag extraData;

    private AutomationContext(Builder builder) {
        this.level = builder.level;
        this.player = builder.player;
        this.momentInstance = builder.momentInstance;
        this.extraData = builder.extraData;
    }

    public Level getLevel() {
        return level;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    public Optional<MomentInstance> getMomentInstance() {
        return Optional.ofNullable(momentInstance);
    }

    public boolean hasData(String key) {
        return extraData.contains(key);
    }

    public CompoundTag getExtraData() {
        return extraData.copy();
    }

    public Optional<Integer> getInt(String key) {
        return extraData.contains(key, Tag.TAG_INT) ?
                Optional.of(extraData.getInt(key)) : Optional.empty();
    }

    public Optional<String> getString(String key) {
        return extraData.contains(key, Tag.TAG_STRING) ?
                Optional.of(extraData.getString(key)) : Optional.empty();
    }

    public Optional<Boolean> getBoolean(String key) {
        return extraData.contains(key, Tag.TAG_BYTE) ?
                Optional.of(extraData.getBoolean(key)) : Optional.empty();
    }

    public Optional<ItemStack> getItemStack(String key) {
        return extraData.contains(key, Tag.TAG_COMPOUND) ?
            ItemStack.parse(level.registryAccess(),extraData.getCompound(key)) : Optional.empty();
    }

    public Optional<BlockPos> getBlockPos(String key) {
        return extraData.contains(key, Tag.TAG_COMPOUND) ?
                Optional.ofNullable(BlockPos.CODEC.decode(NbtOps.INSTANCE, extraData.get(key)).getOrThrow().getFirst()) : Optional.empty();
    }

    public Optional<BlockPos> getBlockPos() {
        return extraData.contains(KEY_POS, Tag.TAG_INT_ARRAY) ?
                Optional.ofNullable(BlockPos.CODEC.decode(NbtOps.INSTANCE, extraData.get(KEY_POS)).getOrThrow().getFirst()) : Optional.empty();
    }

    public Optional<BlockState> getBlockState(String key) {
        return extraData.contains(key, Tag.TAG_COMPOUND) ?
                Optional.ofNullable(BlockState.CODEC.decode(NbtOps.INSTANCE, extraData.get(key)).getOrThrow().getFirst()) : Optional.empty();
    }

    public Optional<Block> getBlock(String key) {
        return extraData.contains(key, Tag.TAG_COMPOUND) ?
                Optional.ofNullable(Block.CODEC.codec().decode(NbtOps.INSTANCE, extraData.get(key)).getOrThrow().getFirst()) : Optional.empty();
    }

    public Optional<Block> getBlock() {
        return extraData.contains(KEY_BLOCK, Tag.TAG_COMPOUND) ?
                Optional.ofNullable(Block.CODEC.codec().decode(NbtOps.INSTANCE, extraData.get(KEY_BLOCK)).getOrThrow().getFirst()) : Optional.empty();
    }

    public Optional<EntityType<?>> getEntityType(String key) {
        return extraData.contains(key, Tag.TAG_STRING) ?
                EntityType.byString(extraData.getString(key)) : Optional.empty();
    }

    public Optional<EntityType<?>> getEntityType() {
        return extraData.contains(KEY_ENTITY_TYPE, Tag.TAG_STRING) ?
                EntityType.byString(extraData.getString(KEY_ENTITY_TYPE)) : Optional.empty();
    }

    public Optional<Difficulty> getDifficulty(String key) {
        return extraData.contains(key, Tag.TAG_STRING) ?
                Optional.ofNullable(Difficulty.byName(extraData.getString(key))) : Optional.empty();
    }

    public Optional<Difficulty> getDifficulty() {
        return extraData.contains(KEY_DIFFICULTY, Tag.TAG_STRING) ?
                Optional.ofNullable(Difficulty.byName(extraData.getString(KEY_DIFFICULTY))) : Optional.empty();
    }

    public Optional<Long> getCurrentGameTime() {
        return extraData.contains(KEY_CURRENT_GAME_TIME, Tag.TAG_LONG) ?
                Optional.of(extraData.getLong(KEY_CURRENT_GAME_TIME)) : Optional.empty();
    }

    public Optional<Long> getCurrentDayTime() {
        return extraData.contains(KEY_CURRENT_DAY_TIME, Tag.TAG_LONG) ?
                Optional.of(extraData.getLong(KEY_CURRENT_DAY_TIME)) : Optional.empty();
    }

    public Builder toBuilder() {
        Builder builder = new Builder(this.level);
        builder.player = this.player;
        builder.momentInstance = this.momentInstance;
        builder.extraData = this.extraData.copy();
        return builder;
    }

    public static class Builder {
        private final Level level;
        private Player player;
        private CompoundTag extraData = new CompoundTag();
        private MomentInstance momentInstance;

        public Builder(Level level) {
            this.level = level;
        }

        public Builder addPlayer(Player player) {
            this.player = player;
            return this;
        }

        public Builder addNBT(String key, Tag nbt) {
            this.extraData.put(key, nbt);
            return this;
        }

        public Builder addInt(String key, int value) {
            this.extraData.putInt(key, value);
            return this;
        }

        public Builder addString(String key, String value) {
            this.extraData.putString(key, value);
            return this;
        }

        public Builder addBoolean(String key, boolean value) {
            this.extraData.putBoolean(key, value);
            return this;
        }

        public Builder addMomentInstance(MomentInstance momentInstance) {
            this.momentInstance = momentInstance;
            return this;
        }

        public Builder addItemStack(String key, ItemStack stack) {
            this.extraData.put(key, stack.save(level.registryAccess(),new CompoundTag()));
            return this;
        }

        public Builder addBlockPos(String key, BlockPos pos) {
            this.extraData.put(key, BlockPos.CODEC.encodeStart(NbtOps.INSTANCE,pos).getOrThrow());
            return this;
        }

        public Builder addBlockPos(BlockPos pos) {
            if (pos != null){
                this.extraData.put(KEY_POS, BlockPos.CODEC.encodeStart(NbtOps.INSTANCE,pos).getOrThrow());
            }
            return this;
        }

        public Builder addBlockState(String key, BlockState blockState) {
            this.extraData.put(key, BlockState.CODEC.encodeStart(NbtOps.INSTANCE, blockState).getOrThrow());
            return this;
        }

        public Builder addBlock(String key, Block block) {
            this.extraData.put(key, Block.CODEC.codec().encodeStart(NbtOps.INSTANCE, block).getOrThrow());
            return this;
        }

        public Builder addBlock(Block block) {
            this.extraData.put(KEY_BLOCK, Block.CODEC.codec().encodeStart(NbtOps.INSTANCE, block).getOrThrow());
            return this;
        }

        public Builder addEntityType(String key, EntityType<?> entityType) {
            this.extraData.putString(key,EntityType.getKey(entityType).toString());
            return this;
        }

        public Builder addEntityType(EntityType<?> entityType) {
            this.extraData.putString(KEY_ENTITY_TYPE,EntityType.getKey(entityType).toString());
            return this;
        }

        public Builder addDifficulty(String key,Difficulty difficulty) {
            this.extraData.putString(key, difficulty.getKey());
            return this;
        }


        public Builder addDifficulty(Difficulty difficulty) {
            this.extraData.putString(KEY_DIFFICULTY, difficulty.getKey());
            return this;
        }

        public Builder addCurrentGameTime(long currentGameTime) {
            this.extraData.putLong(KEY_CURRENT_GAME_TIME, currentGameTime);
            return this;
        }

        public Builder addCurrentDayTime(long currentDayTime) {
            this.extraData.putLong(KEY_CURRENT_DAY_TIME, currentDayTime);
            return this;
        }

        public AutomationContext build() {
            return new AutomationContext(this);
        }
    }
}
