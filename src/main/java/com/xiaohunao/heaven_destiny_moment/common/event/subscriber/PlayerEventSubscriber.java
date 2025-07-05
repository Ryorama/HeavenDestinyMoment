package com.xiaohunao.heaven_destiny_moment.common.event.subscriber;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
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
            PacketDistributor.sendToPlayer((ServerPlayer)player, new MomentManagerSyncPayload(instance.serializeNBTWithoutEnemiesManager(),false));
            if (instance.getBar() != null) {
                instance.getBar().addBar();
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

//        try {
//            MomentTypeKubeJSBuilder.CanSpawnEntityCallback callback = (instance, level1, entity, pos) -> {
//                System.out.println("Checking spawn at: " + pos);
//                return true;
//            };
//
//            CallbackMetadata metadata = CallbackMetadata.serialize(callback);
//            CallbackMetadata.CODEC.encodeStart(JsonOps.INSTANCE, metadata).result().ifPresent(jsonElement -> {
//                System.out.println("Serialized: " + jsonElement);
//                CallbackMetadata.CODEC.parse(JsonOps.INSTANCE, jsonElement).result().ifPresent(callbackMetadata -> {
//                    try {
//                        MomentTypeKubeJSBuilder.CanSpawnEntityCallback deserialize =
//                            (MomentTypeKubeJSBuilder.CanSpawnEntityCallback) CallbackMetadata.deserialize(callbackMetadata);
//                        deserialize.canSpawnEntity(null, null, null, event.getPos());
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}