package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.attachment.KillEntityRecorderAttachment;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMAttachments;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
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

public record KillEntityRecorderSyncPayload(KillEntityRecorderAttachment.KillType killType, UUID uuid, KillEntityRecorderAttachment attachment) implements CustomPacketPayload {
    public static final Type<KillEntityRecorderSyncPayload> TYPE = new Type<>(HeavenDestinyMoment.asResource("kill_entity_required_sync"));
    public static final StreamCodec<ByteBuf, KillEntityRecorderSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(KillEntityRecorderAttachment.KillType.CODEC), KillEntityRecorderSyncPayload::killType,
            UUIDUtil.STREAM_CODEC, KillEntityRecorderSyncPayload::uuid,
            ByteBufCodecs.fromCodec(KillEntityRecorderAttachment.CODEC), KillEntityRecorderSyncPayload::attachment,
            KillEntityRecorderSyncPayload::new
    );


    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Level level = context.player().level();
                if (killType == KillEntityRecorderAttachment.KillType.MOMENT) {
                    MomentInstanceManager manager = MomentInstanceManager.of(level);
                    manager.getMomentInstance(uuid).setData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER, attachment);
                }

                if (killType == KillEntityRecorderAttachment.KillType.PLAYER) {
                    context.player().setData(HDMAttachments.MOMENT_KILL_ENTITY_RECORDER, attachment);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}
