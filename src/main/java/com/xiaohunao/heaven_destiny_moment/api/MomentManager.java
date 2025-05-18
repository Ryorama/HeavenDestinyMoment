package com.xiaohunao.heaven_destiny_moment.api;

import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.DynamicSerializerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MomentManager extends SimpleDynamicLoader<Moment> {
    private static final MomentManager INSTANCE = new MomentManager();
    private static final String FOLDER = "heaven_destiny_moment/moment";

    private MomentManager() {
        super(FOLDER, HDMRegistries.MOMENT, DynamicSerializerType.of(IMoment.CODEC));
    }

    public static MomentManager getInstance(){
        return INSTANCE;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        super.apply(resources, resourceManager, profiler);

        HDMRegistries.MOMENT.stream().forEach(moment -> {
            moment.momentData().flatMap(MomentData::stateSettingsGroup).ifPresent(stateSettingsGroup ->{
                stateSettingsGroup.states().forEach((state, triggersContext) -> {
                    TriggerType<?> triggerType = TriggerTypeManager.TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.inverse().get(triggersContext.trigger().getClass());
                    if (state == MomentState.CREATE){
                        TriggerTypeManager.CREATE_TRIGGER_TYPE_MOMENT_MULTIMAP.put(triggerType, moment);
                        return;
                    }
                    TriggerTypeManager.STATE_TRIGGER_TYPE_MOMENT_MULTIMAP.put(triggerType, moment);
                });
            });
        });
    }
}
