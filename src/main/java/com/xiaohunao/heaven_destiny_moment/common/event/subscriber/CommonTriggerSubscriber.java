package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationThreadManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class CommonTriggerSubscriber {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

        for (ServerPlayer serverPlayer : serverLevel.players()) {
            AutomationContext context = new AutomationContext.Builder(serverLevel)
                    .addPlayer(serverPlayer)
                    .addBlockPos(serverPlayer.blockPosition())
                    .addCurrentDayTime(level.getDayTime())
                    .addCurrentGameTime(level.getGameTime())
                    .build();

            momentInstanceManager.trigger(RandomLevelTickTrigger.class, context);
            momentInstanceManager.trigger(LevelTickTrigger.class, context);
            momentInstanceManager.trigger(TimeProbabilityTrigger.class, context);
        }

        AutomationThreadManager.getInstance().executePendingTasks();
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) return;

        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of((Level) level);

        momentInstanceManager.trigger(BlockBreakTrigger.class,
                new AutomationContext.Builder((ServerLevel) level)
                        .addBlock(level.getBlockState(event.getPos()).getBlock())
                        .build()
        );

        AutomationThreadManager.getInstance().executePendingTasks();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide) return;
        ServerPlayer serverPlayer = event.getSource() == null ? null : event.getSource().getEntity() instanceof ServerPlayer player ? player : null;

        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

        momentInstanceManager.trigger(KillEntityTrigger.class,
                new AutomationContext.Builder(level)
                        .addEntityType(victim.getType())
                        .addPlayer(serverPlayer)
                        .build()
        );

        AutomationThreadManager.getInstance().executePendingTasks();
    }
}
