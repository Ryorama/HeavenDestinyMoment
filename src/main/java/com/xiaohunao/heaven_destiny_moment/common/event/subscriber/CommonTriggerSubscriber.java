package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMTriggerTypes;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.KillAnyEntityTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.LevelTickTrigger;
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


        if (level.isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            TriggerTypeManager.trigger(HDMTriggerTypes.RANDOM_LEVEL_TICK.get(), level, trigger -> trigger.canTrigger(level), serverPlayer.blockPosition(), serverPlayer);
            TriggerTypeManager.trigger(HDMTriggerTypes.LEVEL_TICK.get(), level, LevelTickTrigger::canTrigger, serverPlayer.blockPosition(), serverPlayer);
            TriggerTypeManager.trigger(HDMTriggerTypes.TIME_PROBABILITY.get(), level, trigger -> trigger.canTrigger(level), serverPlayer.blockPosition(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();

        TriggerTypeManager.trigger(HDMTriggerTypes.BLOCK_BREAK.get(), (Level) level, trigger -> trigger.canTrigger((Level) level, event.getPos()), event.getPos(), (ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        ServerPlayer serverPlayer = event.getSource() == null ? null : event.getSource().getEntity() instanceof ServerPlayer player ? player : null;

        TriggerTypeManager.trigger(HDMTriggerTypes.KILL_ANY_ENTITY_COMMON.get(), victim.level(), KillAnyEntityTrigger::canTrigger, victim.blockPosition(), serverPlayer);
        TriggerTypeManager.trigger(HDMTriggerTypes.KILL_ENTITY_COMMON.get(), victim.level(), trigger -> trigger.canTrigger(victim.getType()), victim.blockPosition(), serverPlayer);
    }

}
