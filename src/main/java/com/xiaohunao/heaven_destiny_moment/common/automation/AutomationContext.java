package com.xiaohunao.heaven_destiny_moment.common.automation;

import com.google.common.collect.Maps;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
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
    private final Map<String, Object> extraData;

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
        return extraData.containsKey(key);
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public Optional<Integer> getInt(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof Integer v ? v : null);
    }

    public Optional<String> getString(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof String v ? v : null);
    }

    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof Boolean v ? v : null);
    }

    public Optional<ItemStack> getItemStack(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof ItemStack v ? v : null);
    }

    public Optional<BlockPos> getBlockPos(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof BlockPos v ? v : null);
    }

    public Optional<BlockPos> getBlockPos() {
        return getBlockPos(KEY_POS);
    }

    public Optional<BlockState> getBlockState(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof BlockState v ? v : null);
    }

    public Optional<Block> getBlock(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof Block v ? v : null);
    }

    public Optional<Block> getBlock() {
        return getBlock(KEY_BLOCK);
    }

    public Optional<EntityType<?>> getEntityType(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof EntityType<?> v ? v : null);
    }

    public Optional<EntityType<?>> getEntityType() {
        return getEntityType(KEY_ENTITY_TYPE);
    }

    public Optional<Difficulty> getDifficulty(String key) {
        return Optional.ofNullable(extraData.get(key) instanceof Difficulty v ? v : null);
    }

    public Optional<Difficulty> getDifficulty() {
        return Optional.ofNullable(extraData.get(KEY_DIFFICULTY) instanceof Difficulty v ? v : null);
    }

    public Optional<Long> getCurrentGameTime() {
        return Optional.ofNullable(extraData.get(KEY_CURRENT_GAME_TIME) instanceof Long v ? v : null);
    }

    public Optional<Long> getCurrentDayTime() {
        return Optional.ofNullable(extraData.get(KEY_CURRENT_DAY_TIME) instanceof Long v ? v : null);
    }

    public Builder toBuilder() {
        Builder builder = new Builder(this.level);
        builder.player = this.player;
        builder.momentInstance = this.momentInstance;
        builder.extraData = Maps.newHashMap(this.extraData);
        return builder;
    }

    public static class Builder {
        private final Level level;
        private Player player;
        private Map<String, Object> extraData = Maps.newHashMap();
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
            this.extraData.put(key, value);
            return this;
        }

        public Builder addString(String key, String value) {
            this.extraData.put(key, value);
            return this;
        }

        public Builder addBoolean(String key, boolean value) {
            this.extraData.put(key, value);
            return this;
        }

        public Builder addMomentInstance(MomentInstance momentInstance) {
            this.momentInstance = momentInstance;
            return this;
        }

        public Builder addItemStack(String key, ItemStack stack) {
            this.extraData.put(key, stack.save(level.registryAccess(), new CompoundTag()));
            return this;
        }

        public Builder addBlockPos(String key, BlockPos pos) {
            this.extraData.put(key, pos);
            return this;
        }

        public Builder addBlockPos(BlockPos pos) {
            if (pos != null) {
                this.extraData.put(KEY_POS, pos);
            }
            return this;
        }

        public Builder addBlockState(String key, BlockState blockState) {
            this.extraData.put(key, blockState);
            return this;
        }

        public Builder addBlock(String key, Block block) {
            this.extraData.put(key, block);
            return this;
        }

        public Builder addBlock(Block block) {
            this.extraData.put(KEY_BLOCK, block);
            return this;
        }

        public Builder addEntityType(String key, EntityType<?> entityType) {
            this.extraData.put(key, entityType);
            return this;
        }

        public Builder addEntityType(EntityType<?> entityType) {
            this.extraData.put(KEY_ENTITY_TYPE, entityType);
            return this;
        }

        public Builder addDifficulty(String key, Difficulty difficulty) {
            this.extraData.put(key, difficulty);
            return this;
        }


        public Builder addDifficulty(Difficulty difficulty) {
            this.extraData.put(KEY_DIFFICULTY, difficulty);
            return this;
        }

        public Builder addCurrentGameTime(long currentGameTime) {
            this.extraData.put(KEY_CURRENT_GAME_TIME, currentGameTime);
            return this;
        }

        public Builder addCurrentDayTime(long currentDayTime) {
            this.extraData.put(KEY_CURRENT_DAY_TIME, currentDayTime);
            return this;
        }

        public AutomationContext build() {
            return new AutomationContext(this);
        }
    }
}
