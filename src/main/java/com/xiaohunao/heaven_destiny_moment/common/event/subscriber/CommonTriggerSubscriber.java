package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class CommonTriggerSubscriber {
    @SubscribeEvent
    public static void onBlockBreaks(BlockEvent.BreakEvent event) {
    }


}
