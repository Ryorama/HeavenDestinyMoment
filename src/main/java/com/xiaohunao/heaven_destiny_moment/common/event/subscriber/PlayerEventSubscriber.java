package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.network.MomentManagerSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = HeavenDestinyMoment.MODID)
public class PlayerEventSubscriber {
    @SubscribeEvent
    public static void onClientPlayerNetworkLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        MomentBarOverlay.barMap.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(player.level());

        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            instance.updatePlayers();
            PacketDistributor.sendToPlayer((ServerPlayer)player, new MomentManagerSyncPayload(instance.serializeNBT(),false));
            MomentBar bar = instance.getBar();
            if (bar != null) {
                bar.addBar();
                for (Player player1 : bar.getPlayers()) {
                    bar.addPlayer(player1);
                }
            }

        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(player.level());
        for (MomentInstance instance : momentInstanceManager.getMomentInstances()) {
            instance.updatePlayers();
            PacketDistributor.sendToPlayer((ServerPlayer)player, new MomentManagerSyncPayload(instance.serializeNBT(),false));
            MomentBar bar = instance.getBar();
            if (bar != null) {
                bar.addBar();
                for (Player player1 : bar.getPlayers()) {
                    bar.addPlayer(player1);
                }
            }
        }
    }


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