package com.xiaohunao.heaven_destiny_moment.common.network;

import com.mojang.serialization.Codec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.MomentBar;
import com.xiaohunao.heaven_destiny_moment.client.gui.hud.MomentBarOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public record MomentBarSyncPayload(MomentBar bar,  SyncType syncType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MomentBarSyncPayload> TYPE = new CustomPacketPayload.Type<>(HeavenDestinyMoment.asResource("moment_bar_sync"));

    public static final StreamCodec<ByteBuf, MomentBarSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(MomentBar.CODEC), MomentBarSyncPayload::bar,
            ByteBufCodecs.fromCodec(SyncType.CODEC),MomentBarSyncPayload::syncType,
            MomentBarSyncPayload::new
    );

    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更严格的客户端检查
            if (!context.flow().isClientbound()) {
                HeavenDestinyMoment.LOGGER.warn("Received client-only packet on server side!");
                return;
            }

            if (context.player() == null || !context.player().isLocalPlayer()) {
                return;
            }

            Map<UUID, MomentBar> barMap = MomentBarOverlay.barMap;

            // 添加空值检查
            if (bar == null || bar.getID() == null) {
                HeavenDestinyMoment.LOGGER.warn("Received invalid MomentBar data");
                return;
            }
            MomentBar momentBar = barMap.get(bar.getID());


            switch (syncType) {
                case ADD_BAR -> {
                    if (momentBar == null){
                        barMap.put(bar.getID(), bar);
                    }
                }
                case REMOVE_BAR -> {
                    barMap.remove(bar.getID());
                }
                case UPDATE_PROGRESS -> {
                    if (momentBar != null) {
                        momentBar.updateProgress(context.player().level(),bar.getProgress());
                    }
                }
                case ADD_PLAYER -> {
                    if (momentBar != null) {
                        momentBar.addPlayer(context.player());
                    }
                }
                case REMOVE_PLAYER -> {
                    if (momentBar != null) {
                        momentBar.removePlayer(context.player());
                    }
                }
            }
        }).exceptionally(e -> {
            HeavenDestinyMoment.LOGGER.error("Error handling MomentBarSyncPayload", e);
            context.disconnect(Component.translatable("heaven_destiny_moment.network.sync_error"));
            return null;
        });
    }

    public static MomentBarSyncPayload addPlayer(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.ADD_PLAYER);
    }

    public static MomentBarSyncPayload removePlayer(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.REMOVE_PLAYER);
    }

    public static MomentBarSyncPayload addBar(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.ADD_BAR);
    }

    public static MomentBarSyncPayload removeBar(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.REMOVE_BAR);
    }

    public static MomentBarSyncPayload updateProgress(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.UPDATE_PROGRESS);
    }



    public enum SyncType {
        ADD_BAR,
        ADD_PLAYER,
        REMOVE_BAR,
        REMOVE_PLAYER,
        UPDATE_PROGRESS;

        public static final Codec<SyncType> CODEC = Codec.STRING.xmap(type -> valueOf(type.toUpperCase(Locale.ROOT)), type -> type.name().toLowerCase(Locale.ROOT));
    }
}
