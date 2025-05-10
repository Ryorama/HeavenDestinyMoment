package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.*;
import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.StateSettingsGroup;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.DynamicSerializerType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TriggerTypeManager extends SimpleDynamicLoader<TriggerType<?>> {
    private static final TriggerTypeManager INSTANCE = new TriggerTypeManager();
    private static final String FOLDER = "heaven_destiny_moment/trigger_type";

    public static final Multimap<TriggerType<?>,Moment> TRIGGER_TYPE_MOMENT_MULTIMAP = HashMultimap.create();
    public static final BiMap<TriggerType<?>,Class<? extends ITrigger>> TRIGGER_TYPE_TRIGGER_CLASS_BIMAP = HashBiMap.create();

    private TriggerTypeManager() {
        super(FOLDER, HDMRegistries.TRIGGER_TYPE, DynamicSerializerType.of(TriggerType.CODEC));
    }

    public static TriggerTypeManager getInstance(){
        return INSTANCE;
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        TRIGGER_TYPE_MOMENT_MULTIMAP.clear();
        TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.clear();
        super.apply(resources, resourceManager, profiler);

        HDMRegistries.TRIGGER_TYPE.stream().forEach(triggerType -> {
            TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.put(triggerType, triggerType.getTriggerClass());
        });
    }

    public static <T extends ITrigger> void trigger(TriggerType<?> triggerType, Level level, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        Collection<Moment> moments = TRIGGER_TYPE_MOMENT_MULTIMAP.get(triggerType);
        MomentInstanceManager instanceManager = MomentInstanceManager.of(level);

        moments.forEach(moment -> {
            moment.momentData().flatMap(MomentData::stateSettingsGroup).flatMap(StateSettingsGroup::creates).ifPresent(creates -> {
                instanceManager.createMomentInstance(moment, pos, serverPlayer);
            });
        });

        for (MomentInstance momentInstance : instanceManager.getMomentInstances()) {
            momentInstance.getMoment().momentData.flatMap(MomentData::stateSettingsGroup).flatMap(StateSettingsGroup::states).ifPresent(statemultimap -> {
                statemultimap.asMap().forEach(((state, conditionalTriggers) -> {
                    boolean triggerMatch = conditionalTriggers.stream().allMatch(conditionalTrigger -> conditionalTrigger.trigger().canTrigger(momentInstance, pos, serverPlayer));

                    boolean conditionalMatch = conditionalTriggers.stream().flatMap(conditionalTrigger -> conditionalTrigger.conditions().stream())
                            .allMatch(condition -> condition.matches(momentInstance, pos, serverPlayer));

                    if (triggerMatch && conditionalMatch) {
                        momentInstance.setState(state);
                    }
                }));
            });
        }
    }

}
