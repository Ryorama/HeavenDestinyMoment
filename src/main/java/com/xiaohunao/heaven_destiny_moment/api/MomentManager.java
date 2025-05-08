package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.DynamicSerializerType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
                stateSettingsGroup.creates().map(List::stream).ifPresent(stream -> {
                    stream.forEach(creates -> {
                        TriggerType<?> triggerType = TriggerTypeManager.TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.inverse().get(creates.trigger().getClass());
                        TriggerTypeManager.TRIGGER_TYPE_MOMENT_MULTIMAP.put(triggerType, moment);
                    });
                });

                stateSettingsGroup.states()
                        .map(Multimap::asMap).ifPresent(map -> {
                            map.forEach((state, conditionalTriggers) -> {
                                conditionalTriggers.forEach(conditionalTrigger -> {
                                    TriggerType<?> triggerType = TriggerTypeManager.TRIGGER_TYPE_TRIGGER_CLASS_BIMAP.inverse().get(conditionalTrigger.trigger().getClass());
                                    TriggerTypeManager.TRIGGER_TYPE_MOMENT_MULTIMAP.put(triggerType, moment);
                                });
                            });
                        });
            });
        });
    }
}
