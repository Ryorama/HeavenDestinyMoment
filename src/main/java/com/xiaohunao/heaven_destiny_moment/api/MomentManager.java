package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.actuator.ActuatorContext;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
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

        TriggerTypeManager triggerTypeManager = TriggerTypeManager.getInstance();
        triggerTypeManager.clear();

        Map<Class<? extends ITrigger>,TriggerType<?>> triggerTypeMomentMap = Maps.newHashMap();
        for (TriggerType<?> triggerType : HDMRegistries.TRIGGER_TYPE) {
            triggerTypeMomentMap.put(triggerType.clazz(), triggerType);
        }

        HDMRegistries.MOMENT.stream().forEach(moment -> {
            moment.momentData().flatMap(MomentData::autoActuatorGroupSettings).map(AutoActuatorGroupSettings::autoActuators).ifPresent(map -> {
                map.forEach((triggerContext, actuatorContext) -> {
                    TriggerType<?> triggerType = triggerTypeMomentMap.get(triggerContext.trigger().getClass());
                    if (triggerType != null) {
                        triggerTypeManager.add(triggerType, moment);
                    }
                });
            });
        });
    }
}
