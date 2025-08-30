package com.xiaohunao.heaven_destiny_moment.api;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.xhn_lib.api.data.loader.BaseDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.IDynamicSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MomentManager extends BaseDynamicLoader<IMoment> {
    private static final MomentManager INSTANCE = new MomentManager();
    private static final String FOLDER = "heaven_destiny_moment/moment";

    private final Multimap<Class<?>, Pair<IMoment,AutomationRule>> triggerMap = ArrayListMultimap.create();

    private MomentManager() {
        super(FOLDER, HDMRegistries.MOMENT, IDynamicSerializer.of(IMoment.CODEC));
    }

    public static MomentManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void onAfterRegister(ResourceLocation location, IMoment moment) {
        moment.momentData()
                .flatMap(MomentData::autoActuatorGroupSettings)
                .flatMap(AutoActuatorGroupSettings::createRule)
                .ifPresent(createRule -> {
                    createRule.trigger().ifPresent(trigger -> triggerMap.put(trigger.getClass(), new Pair<>(moment,createRule)));
                });
    }

    @Override
    protected void onValueRemoved(ResourceLocation location) {
        IMoment moment = loadedValues.get(location);
        if (moment != null) {
            triggerMap.values().removeIf(pair -> pair.getFirst().equals(moment));
        }
    }

    public Collection<Pair<IMoment, AutomationRule>> getRulesTriggerType(Class<?> triggerType) {
        return triggerMap.get(triggerType);
    }
}
