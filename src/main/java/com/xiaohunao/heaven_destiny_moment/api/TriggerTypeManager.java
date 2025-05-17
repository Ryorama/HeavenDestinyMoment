package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.*;
import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.StateSettingsGroup;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ConditionalTrigger;
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

    public static final Multimap<TriggerType<?>,Moment> CREATE_TRIGGER_TYPE_MOMENT_MULTIMAP = HashMultimap.create();
    public static final Multimap<TriggerType<?>,Moment> STATE_TRIGGER_TYPE_MOMENT_MULTIMAP = HashMultimap.create();
    public static final BiMap<TriggerType<?>,Class<? extends ITrigger>> TRIGGER_TYPE_TRIGGER_CLASS_BIMAP = HashBiMap.create();

    private TriggerTypeManager() {
        super(FOLDER, HDMRegistries.TRIGGER_TYPE, DynamicSerializerType.of(TriggerType.CODEC));
    }

    public static TriggerTypeManager getInstance(){
        return INSTANCE;
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        STATE_TRIGGER_TYPE_MOMENT_MULTIMAP.clear();
        CREATE_TRIGGER_TYPE_MOMENT_MULTIMAP.clear();
        TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.clear();
        super.apply(resources, resourceManager, profiler);

        HDMRegistries.TRIGGER_TYPE.stream().forEach(triggerType -> {
            TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.put(triggerType, triggerType.getTriggerClass());
        });
    }

    public static <T extends ITrigger> void trigger(TriggerType<T> triggerType, Level level, ICanTrigger<T> iCanTrigger, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer){
        MomentInstanceManager instanceManager = MomentInstanceManager.of(level);
        CREATE_TRIGGER_TYPE_MOMENT_MULTIMAP.get(triggerType).forEach(moment -> {
            moment.momentData().flatMap(MomentData::stateSettingsGroup).map(StateSettingsGroup::states).map(state -> state.get(MomentState.CREATE)).ifPresent(create -> {
                if (iCanTrigger.canTrigger((T) create.trigger())) {
                    instanceManager.createMomentInstance(moment, pos, serverPlayer);
                }
            });
        });

        for (MomentInstance momentInstance : instanceManager.getMomentInstances()) {
            STATE_TRIGGER_TYPE_MOMENT_MULTIMAP.get(triggerType).forEach(moment -> {
                if (moment == momentInstance.getMoment()) {
                    momentInstance.getMoment().momentData.flatMap(MomentData::stateSettingsGroup).map(StateSettingsGroup::states).ifPresent(statemultimap -> {
                        statemultimap.forEach(((state, conditionalTriggers) -> {
                            if (state == MomentState.CREATE){
                                return;
                            }

                            boolean canTrigger = true;
                            boolean hasCondition = true;

                            if (!iCanTrigger.canTrigger((T) conditionalTriggers.trigger())){
                                canTrigger = false;

                            }

                            for (ICondition condition : conditionalTriggers.conditions()) {
                                if (!condition.matches(momentInstance, state, pos, serverPlayer)) {
                                    hasCondition = false;
                                    break;
                                }
                            }

                            if (canTrigger && hasCondition) {
                                momentInstance.setState(state);
                            }

                        }));
                    });
                }
            });
        }

    }

    @FunctionalInterface
    public interface ICanTrigger<T extends ITrigger> {
        boolean canTrigger(T trigger);
    }

}
