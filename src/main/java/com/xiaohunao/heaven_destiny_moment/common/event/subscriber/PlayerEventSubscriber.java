package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.moment.EnemiesManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.UUID;


@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = HeavenDestinyMoment.MODID)
public class PlayerEventSubscriber {
    @SubscribeEvent
    public static void onPlayerInteractRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        InteractionHand hand = event.getHand();
        Player player = event.getEntity();
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }
    }
}