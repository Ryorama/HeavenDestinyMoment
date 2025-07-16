package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.ConditionalTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MomentInstanceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MomentInstanceBuilder.class);
    private final Moment moment;
    private final Level level;

    private BlockPos pos;
    private ServerPlayer serverPlayer;
    private Consumer<MomentInstance> modifier;
    private boolean checkConditions = true;
    private final List<ICondition> specialConditions = new ArrayList<>();
    
    private MomentInstanceBuilder(Level level,Moment moment) {
        this.moment = Objects.requireNonNull(moment, "Moment cannot be null");
        this.level = Objects.requireNonNull(level, "Level cannot be null");
    }
    
    public static MomentInstanceBuilder builder(Level level,Moment moment) {
        return new MomentInstanceBuilder(level,moment);
    }
    
    public MomentInstanceBuilder pos(BlockPos pos) {
        this.pos = pos;
        return this;
    }
    
    public MomentInstanceBuilder player(ServerPlayer player) {
        this.serverPlayer = player;
        return this;
    }
    
    public MomentInstanceBuilder modifier(Consumer<MomentInstance> modifier) {
        this.modifier = modifier;
        return this;
    }
    
    public MomentInstanceBuilder skipConditions() {
        this.checkConditions = false;
        return this;
    }
    
    public MomentInstanceBuilder addSpecialCondition(ICondition condition) {
        if (condition != null) {
            this.specialConditions.add(condition);
        }
        return this;
    }
    
    public MomentInstanceBuilder addSpecialConditions(ICondition... conditions) {
        this.checkConditions = false;
        for (ICondition condition : conditions) {
            if (condition != null) {
                this.specialConditions.add(condition);
            }
        }
        return this;
    }

    public MomentInstance build() {
        MomentInstanceManager manager = MomentInstanceManager.of(level);
        return manager.createMomentInstance(this);
    }

    public Moment getMoment() { return moment; }
    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public ServerPlayer getServerPlayer() { return serverPlayer; }
    public Consumer<MomentInstance> getModifier() { return modifier; }
    public boolean isCheckConditions() { return checkConditions; }
    public List<ICondition> getSpecialConditions() { return new ArrayList<>(specialConditions); }

    public static MomentInstance create(Level level, Moment moment) {
        return builder(level, moment).build();
    }

    public static MomentInstance create(Level level, Moment moment, BlockPos pos) {
        return builder(level, moment).pos(pos).build();
    }

    public static MomentInstance create(Level level, Moment moment, ServerPlayer player) {
        return builder(level, moment).player(player).build();
    }

    public static MomentInstance create(Level level, Moment moment,BlockPos pos, ServerPlayer player) {
        return builder(level, moment).player(player).pos(pos).build();
    }

    public static MomentInstance skipConditionsExcept(Level level, Moment moment,ICondition... specialConditions) {
        return builder(level, moment).addSpecialConditions(specialConditions).build();
    }

    public static MomentInstance skipConditionsExcept(Level level, Moment moment, BlockPos pos,ICondition... specialConditions) {
        return builder(level, moment).pos(pos).addSpecialConditions(specialConditions).build();
    }

    public static MomentInstance skipConditionsExcept(Level level, Moment moment, ServerPlayer player,ICondition... specialConditions) {
        return builder(level, moment).player(player).addSpecialConditions(specialConditions).build();
    }

    public static MomentInstance skipConditionsExcept(Level level, Moment moment,BlockPos pos, ServerPlayer player,ICondition... specialConditions) {
        return builder(level, moment).player(player).pos(pos).addSpecialConditions(specialConditions).build();
    }

}