package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MomentStateSyncPayload(UUID uuid, MomentState state) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<MomentStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(HeavenDestinyMoment.asResource("moment_state_sync"));
    public static final StreamCodec<ByteBuf, MomentStateSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MomentStateSyncPayload::uuid,
            ByteBufCodecs.fromCodec(MomentState.CODEC), MomentStateSyncPayload::state,
            MomentStateSyncPayload::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Level level = context.player().level();
                MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);

                MomentInstance momentInstance = momentInstanceManager.getMomentInstance(uuid);
                if (momentInstance != null){
                    momentInstance.setState(state);
                    TipSettings tipSettings = momentInstance.getCacheProvider().getTipSettings();
                    if (tipSettings != null) {
                        tipSettings.playTooltip(context.player(), state);
                    }
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}
