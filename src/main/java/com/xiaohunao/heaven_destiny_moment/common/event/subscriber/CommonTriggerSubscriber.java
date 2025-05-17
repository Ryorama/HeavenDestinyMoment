package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMTriggerTypes;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
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
        ServerLevel serverLevel = (ServerLevel)level;
        serverLevel.getPlayers(serverPlayer -> true).forEach(serverPlayer -> {
            TriggerTypeManager.trigger(HDMTriggerTypes.RANDOM_LEVEL_TICK.get(), level, trigger -> trigger.canTrigger(level), serverPlayer.blockPosition(), serverPlayer);
            TriggerTypeManager.trigger(HDMTriggerTypes.LEVEL_TICK.get(), level, LevelTickTrigger::canTrigger, serverPlayer.blockPosition(), serverPlayer);
        });
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        LevelAccessor level = event.getLevel();

        TriggerTypeManager.trigger(HDMTriggerTypes.BLOCK_BREAK.get(), (Level) level, trigger -> trigger.canTrigger((Level) level, event.getPos()), event.getPos(), (ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        Entity damageSourceEntity = event.getSource().getEntity();
        LivingEntity deathEntity = event.getEntity();

        ServerPlayer serverPlayer = null;
        if (damageSourceEntity instanceof ServerPlayer){
            serverPlayer = (ServerPlayer) damageSourceEntity;
        }

        TriggerTypeManager.trigger(HDMTriggerTypes.KILL_ANY_ENTITY_COMMON.get(), deathEntity.level(), KillAnyEntityTrigger::canTrigger,deathEntity.blockPosition(), serverPlayer);
        TriggerTypeManager.trigger(HDMTriggerTypes.KILL_ENTITY_COMMON.get(), deathEntity.level(), trigger -> trigger.canTrigger(deathEntity.getType()),deathEntity.blockPosition(), serverPlayer);
    }

}
