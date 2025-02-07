package com.xiaohunao.heaven_destiny_moment.common.subscriptor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Map;

public record EventSubscriber(String eventName, Map<ResourceKey<Moment<?>>, Map<MomentState, ICondition>> moments, ConditionType type) {
    public static final MapCodec<EventSubscriber> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("event_name").forGetter(EventSubscriber::eventName),
            Codec.unboundedMap(ResourceKey.codec(HDMRegistries.Keys.MOMENT), Codec.unboundedMap(MomentState.CODEC, ICondition.CODEC)).fieldOf("moments").forGetter(EventSubscriber::moments),
            ConditionType.CODEC.fieldOf("condition_type").forGetter(EventSubscriber::type)
    ).apply(instance, EventSubscriber::new));
    public static final Codec<EventSubscriber> CODEC = MAP_CODEC.codec();

    public void onInvoked(Event event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        List<EventData> datas = type.getData(server, event);
        for (EventData data : datas) {
            MomentManager momentManager = data.momentManager();
            BlockPos blockPos = data.blockPos();
            ServerPlayer serverPlayer = data.serverPlayer();

            for (Map.Entry<ResourceKey<Moment<?>>, Map<MomentState, ICondition>> entry : moments.entrySet()) {
                ResourceKey<Moment<?>> momentKey = entry.getKey();
                for (Map.Entry<MomentState, ICondition> entry1 : entry.getValue().entrySet()) {
                    MomentState momentState = entry1.getKey();
                    ICondition condition = entry1.getValue();
                    for (MomentInstance<?> momentInstance : momentManager.getImmutableRunMoments().values()) {
                        if (momentInstance.is(momentKey) && condition.matches(momentInstance, blockPos, serverPlayer)) {
                            momentInstance.setState(momentState);
                        }
                    }
                }
            }
        }
    }
}
