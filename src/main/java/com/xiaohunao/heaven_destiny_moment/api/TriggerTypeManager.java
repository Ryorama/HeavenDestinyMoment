package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.DynamicSerializerType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TriggerTypeManager extends SimpleDynamicLoader<TriggerType<?>> {
    private static final TriggerTypeManager INSTANCE = new TriggerTypeManager();
    private static final String FOLDER = "heaven_destiny_moment/trigger_type";

    public static final Multimap<TriggerType<?>,Moment> TRIGGER_TYPE_MOMENT_MULTIMAP = HashMultimap.create();

    private static final List<MomentInstance> listen = Lists.newArrayList();

    private TriggerTypeManager() {
        super(FOLDER, HDMRegistries.TRIGGER_TYPE, DynamicSerializerType.of(TriggerType.CODEC));
    }

    public static TriggerTypeManager getInstance(){
        return INSTANCE;
    }

    public <T extends ITrigger> void trigger(TriggerType<T> triggerType, Level level, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        Collection<Moment> moments = TRIGGER_TYPE_MOMENT_MULTIMAP.get(triggerType);

        moments.forEach(moment -> {
            moment.momentData().flatMap(MomentData::stateSettingsGroup).ifPresent(stateSettingsGroup -> {
                stateSettingsGroup.creates().ifPresent(creates -> {
                    MomentInstanceManager.of(level).createMomentInstance(moment, pos, serverPlayer);
                });
            });
        });

        listen.forEach(momentInstance -> {
            momentInstance.getMoment().momentData.flatMap(MomentData::stateSettingsGroup).ifPresent(stateSettingsGroup -> {
                stateSettingsGroup.states().ifPresent(statemultimap -> {
                    statemultimap.asMap().forEach(((state, conditionalTriggers) -> {
                        boolean match = conditionalTriggers.stream().flatMap(conditionalTrigger -> conditionalTrigger.conditions().stream())
                                .allMatch(condition -> condition.matches(momentInstance,pos,serverPlayer));
                        if (match){
                            momentInstance.setState(state);
                        }
                    }));
                });
            });
        });


    }

}
