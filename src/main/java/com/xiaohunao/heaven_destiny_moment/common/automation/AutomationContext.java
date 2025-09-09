package com.xiaohunao.heaven_destiny_moment.common.automation;

import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class AutomationContext {
    private final Level level;
    private Player player;
    private MomentInstance momentInstance;
    private Block block;
    private EntityType<?> entityType;
    private BlockPos pos;
    private Difficulty difficulty;
    private Long currentGameTime;
    private Long currentDayTime;
    private BlockState blockState;
    private ItemStack itemStack;

    private AutomationContext(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public Optional<Player> player() {
        return Optional.ofNullable(player);
    }

    public Optional<MomentInstance> momentInstance() {
        return Optional.ofNullable(momentInstance);
    }

    public Optional<Block> block() {
        return Optional.ofNullable(block);
    }

    public Optional<EntityType<?>> entityType() {
        return Optional.ofNullable(entityType);
    }

    public Optional<BlockPos> blockPos() {
        return Optional.ofNullable(pos);
    }

    public Optional<Difficulty> difficulty() {
        return Optional.ofNullable(difficulty);
    }

    public Optional<Long> currentGameTime() {
        return Optional.ofNullable(currentGameTime);
    }

    public Optional<Long> currentDayTime() {
        return Optional.ofNullable(currentDayTime);
    }

    public Optional<BlockState> blockState() {
        return Optional.ofNullable(blockState);
    }

    public Optional<ItemStack> itemStack() {
        return Optional.ofNullable(itemStack);
    }

    public static AutomationContext of(Level level) {
        return new AutomationContext(level);
    }

    public AutomationContext player(Player player) {
        this.player = player;
        return this;
    }

    public AutomationContext momentInstance(MomentInstance momentInstance) {
        this.momentInstance = momentInstance;
        return this;
    }

    public AutomationContext block(Block block) {
        this.block = block;
        return this;
    }

    public AutomationContext entityType(EntityType<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    public AutomationContext blockPos(BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public AutomationContext difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public AutomationContext currentGameTime(Long currentGameTime) {
        this.currentGameTime = currentGameTime;
        return this;
    }

    public AutomationContext currentDayTime(Long currentDayTime) {
        this.currentDayTime = currentDayTime;
        return this;
    }

    public AutomationContext blockState(BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    public AutomationContext itemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }
}
