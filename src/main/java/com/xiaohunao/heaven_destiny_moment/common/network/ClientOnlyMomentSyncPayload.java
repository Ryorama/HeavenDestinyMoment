package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientOnlyMomentSyncPayload(CompoundTag clientOnlyMoment,boolean isRemove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientOnlyMomentSyncPayload> TYPE = new CustomPacketPayload.Type<>(HeavenDestinyMoment.asResource("client_only_moment_sync"));
    public static final StreamCodec<ByteBuf, ClientOnlyMomentSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CompoundTag.CODEC), ClientOnlyMomentSyncPayload::clientOnlyMoment,
            ByteBufCodecs.BOOL,ClientOnlyMomentSyncPayload::isRemove,
            ClientOnlyMomentSyncPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Level level = context.player().level();
                MomentManager momentManager = MomentManager.of(level);
                MomentInstance<?> momentInstance = MomentInstance.loadStatic(level, clientOnlyMoment);

                if (!isRemove){
                    momentManager.setClientMomentInstance(momentInstance);
                }else {
                    if (momentInstance.isClientOnlyMoment()) {
                        momentManager.setClientMomentInstance(null);
                    }
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}