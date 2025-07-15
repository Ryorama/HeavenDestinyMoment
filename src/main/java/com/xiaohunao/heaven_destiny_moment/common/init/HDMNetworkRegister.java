package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.network.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = HeavenDestinyMoment.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HDMNetworkRegister {
    public static final String VERSION = "0.0.1";

    @SubscribeEvent
    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(MomentManagerSyncPayload.TYPE, MomentManagerSyncPayload.STREAM_CODEC, MomentManagerSyncPayload::handle);
        registrar.playToClient(MomentBarSyncPayload.TYPE, MomentBarSyncPayload.STREAM_CODEC, MomentBarSyncPayload::handle);
        registrar.playToClient(ClientOnlyMomentSyncPayload.TYPE, ClientOnlyMomentSyncPayload.STREAM_CODEC, ClientOnlyMomentSyncPayload::handle);
        registrar.playToClient(KillRequiredSyncPayload.TYPE, KillRequiredSyncPayload.STREAM_CODEC, KillRequiredSyncPayload::handle);
        registrar.playToClient(KillEntityRecorderSyncPayload.TYPE, KillEntityRecorderSyncPayload.STREAM_CODEC, KillEntityRecorderSyncPayload::handle);
        registrar.playToClient(MomentStateSyncPayload.TYPE, MomentStateSyncPayload.STREAM_CODEC, MomentStateSyncPayload::handle);
        registrar.playToClient(MomentUpdatePlayersPayload.TYPE, MomentUpdatePlayersPayload.STREAM_CODEC, MomentUpdatePlayersPayload::handle);
    }
}