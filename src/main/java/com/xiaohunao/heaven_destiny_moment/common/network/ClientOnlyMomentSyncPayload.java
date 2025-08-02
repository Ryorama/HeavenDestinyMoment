package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientOnlyMomentSyncPayload(CompoundTag clientOnlyMoment, boolean isRemove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientOnlyMomentSyncPayload> TYPE = new CustomPacketPayload.Type<>(HeavenDestinyMoment.asResource("client_only_moment_sync"));
    public static final StreamCodec<ByteBuf, ClientOnlyMomentSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CompoundTag.CODEC, NbtAccounter::unlimitedHeap), ClientOnlyMomentSyncPayload::clientOnlyMoment,
            ByteBufCodecs.BOOL, ClientOnlyMomentSyncPayload::isRemove,
            ClientOnlyMomentSyncPayload::new
    );
    

    public static final CompoundTag CLEAR_ALL_TAG = new CompoundTag();
    
    static {
        CLEAR_ALL_TAG.putBoolean("clear_all_client_moments", true);
    }

    public static ClientOnlyMomentSyncPayload clearClientMoments() {
        return new ClientOnlyMomentSyncPayload(CLEAR_ALL_TAG, true);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.isLocalPlayer()) {
                Level level = player.level();
                MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

                if (isRemove || clientOnlyMoment.contains("clear_all_client_moments")){
                    momentInstanceManager.setClientMomentInstance(player,null);
                    return;
                }

                MomentInstance momentInstance = MomentInstance.loadStatic(level, clientOnlyMoment);
                if (momentInstance != null && momentInstance.isClientOnlyMoment()){
                    momentInstanceManager.setClientMomentInstance(player,momentInstance);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}