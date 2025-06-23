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
            if (context.player().isLocalPlayer()) {
                Map<UUID, MomentBar> barMap = MomentBarOverlay.barMap;
                switch (syncType) {
                    case ADD -> barMap.put(bar.getID(), bar);
                    case REMOVE -> barMap.remove(bar.getID());
                    case UPDATE_PROGRESS -> {
                        MomentBar momentBar = barMap.get(bar.getID());
                        if (momentBar == null) return;
                        momentBar.updateProgress(bar.getProgress());
                    }
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }

    public static MomentBarSyncPayload addPlayer(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.ADD);
    }

    public static MomentBarSyncPayload updateProgress(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.UPDATE_PROGRESS);
    }

    public static MomentBarSyncPayload removePlayer(MomentBar bar) {
        return new MomentBarSyncPayload(bar,SyncType.REMOVE);
    }

    public enum SyncType {
        ADD,
        REMOVE,
        UPDATE_PROGRESS;

        public static final Codec<SyncType> CODEC = Codec.STRING.xmap(type -> valueOf(type.toUpperCase(Locale.ROOT)), type -> type.name().toLowerCase(Locale.ROOT));
    }
}
