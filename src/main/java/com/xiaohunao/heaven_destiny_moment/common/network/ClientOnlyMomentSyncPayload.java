package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientOnlyMomentSyncPayload(CompoundTag clientOnlyMoment, boolean isRemove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientOnlyMomentSyncPayload> TYPE = new CustomPacketPayload.Type<>(HeavenDestinyMoment.asResource("client_only_moment_sync"));
    public static final StreamCodec<ByteBuf, ClientOnlyMomentSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CompoundTag.CODEC), ClientOnlyMomentSyncPayload::clientOnlyMoment,
            ByteBufCodecs.BOOL, ClientOnlyMomentSyncPayload::isRemove,
            ClientOnlyMomentSyncPayload::new
    );
    
    // 一个空的NBT标签，用于表示清空所有客户端时刻的特殊情况
    private static final CompoundTag CLEAR_ALL_TAG = new CompoundTag();
    
    static {
        // 添加一个特殊标记到CLEAR_ALL_TAG，以便识别
        CLEAR_ALL_TAG.putBoolean("clear_all_client_moments", true);
    }
    
    // 创建一个清空所有客户端时刻的包
    public static ClientOnlyMomentSyncPayload clearAllClientMoments() {
        return new ClientOnlyMomentSyncPayload(CLEAR_ALL_TAG, true);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Level level = context.player().level();
                MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
                
                // 检查是否为清空所有客户端时刻的特殊情况
                if (isRemove && clientOnlyMoment.contains("clear_all_client_moments")) {
                    // 直接清空客户端时刻，不需要加载具体实例
                    momentInstanceManager.setClientMomentInstance(null);
                } else {
                    // 原有逻辑
                    MomentInstance momentInstance = MomentInstance.loadStatic(level, clientOnlyMoment);

                    if (!isRemove) {
                        momentInstanceManager.setClientMomentInstance(momentInstance);
                    } else {
                        if (momentInstance != null && momentInstance.isClientOnlyMoment()) {
                            momentInstanceManager.setClientMomentInstance(null);
                        }
                    }
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}