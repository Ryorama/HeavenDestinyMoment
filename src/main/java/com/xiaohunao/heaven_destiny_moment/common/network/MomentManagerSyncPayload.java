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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


public record  MomentManagerSyncPayload(CompoundTag runMoment,boolean isRemove) implements CustomPacketPayload {
    public static final Type<MomentManagerSyncPayload> TYPE = new Type<>(HeavenDestinyMoment.asResource("moment_manager_sync"));
    public static final StreamCodec<ByteBuf, MomentManagerSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CompoundTag.CODEC, NbtAccounter::unlimitedHeap), MomentManagerSyncPayload::runMoment,
            ByteBufCodecs.BOOL, MomentManagerSyncPayload::isRemove,
            MomentManagerSyncPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Level level = context.player().level();
                MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(level);
                Optional<MomentInstance> momentInstance = Optional.ofNullable(MomentInstance.loadStatic(level, runMoment));

                if (isRemove){
                    momentInstance.ifPresent(momentInstanceManager::removeMomentInstance);
                }else {
                    momentInstance.ifPresent(momentInstanceManager::addMomentInstance);
                }

            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("neoforge.network.invalid_flow", e.getMessage()));
            return null;
        });
    }
}
