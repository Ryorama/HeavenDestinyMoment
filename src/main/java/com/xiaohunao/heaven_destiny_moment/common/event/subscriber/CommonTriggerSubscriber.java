package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.api.TriggerTypeManager;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMTriggerTypes;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.RandomLevelTickTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
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
            TriggerTypeManager.trigger(HDMTriggerTypes.RANDOM_LEVEL_TICK.get(), level, serverPlayer.blockPosition(), serverPlayer);
            TriggerTypeManager.trigger(HDMTriggerTypes.LEVEL_TICK.get(), level, serverPlayer.blockPosition(), serverPlayer);
        });
    }


}
