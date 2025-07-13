package com.xiaohunao.heaven_destiny_moment.common.network;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record KillRequiredSyncPayload(UUID uuid, IActuator actuator, KillEntityCondition.RequiredKill requiredKill) implements CustomPacketPayload{
    public static final Type<KillRequiredSyncPayload> TYPE = new Type<>(HeavenDestinyMoment.asResource("kill_required_sync"));
    public static final StreamCodec<ByteBuf, KillRequiredSyncPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, KillRequiredSyncPayload::uuid,
            ByteBufCodecs.fromCodec(IActuator.CODEC), KillRequiredSyncPayload::actuator,
            ByteBufCodecs.fromCodec(KillEntityCondition.RequiredKill.CODEC, NbtAccounter::unlimitedHeap), KillRequiredSyncPayload::requiredKill,
            KillRequiredSyncPayload::new
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
                momentInstance.setVictoryRequiredKill(actuator,requiredKill);
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}
