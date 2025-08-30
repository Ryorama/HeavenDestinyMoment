package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationRule;
import com.xiaohunao.heaven_destiny_moment.common.context.AutoActuatorGroupSettings;
import com.xiaohunao.heaven_destiny_moment.common.context.MomentData;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.KillEntityCondition;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.xhn_lib.common.util.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TriggerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerManager.class);
    private final MomentInstance instance;

    protected Map<ResourceLocation, Pair<AutomationRule,Integer>> runtimeAutoActuators = new ConcurrentHashMap<>();
    protected Multimap<Class<?>,AutomationRule> contextMultimap =  Multimaps.synchronizedListMultimap(LinkedListMultimap.create());


    public TriggerManager(MomentInstance instance) {
        this.instance = instance;
        init();
    }

    public Map<ResourceLocation, Pair<AutomationRule, Integer>> getRuntimeAutoActuators() {
        return runtimeAutoActuators;
    }

    public Multimap<Class<?>, AutomationRule> getContextMultimap() {
        return contextMultimap;
    }

    public Pair<AutomationRule,Integer> getRuntimeAutoActuator(ResourceLocation name) {
        return runtimeAutoActuators.get(name);
    }


    public void init() {
        AutoActuatorGroupSettings autoActuatorGroupSettings = instance.getCacheProvider().getAutoActuatorGroupSettings();
        if (autoActuatorGroupSettings != null) {
            autoActuatorGroupSettings.runtimeRules().ifPresent(runtimeRules -> {
                runtimeRules.forEach((rule) -> {
                    runtimeAutoActuators.put(rule.name(), new Pair<>(rule, rule.maxExecutionCount().getAmount()));

                    rule.trigger().ifPresent(trigger -> {
                        contextMultimap.put(trigger.getClass(), rule);
                    });
                });
            });
        }
    }

    public <T extends ITrigger> void trigger(Class<T> triggerClass, AutomationContext context) {
        Collection<AutomationRule> rules = contextMultimap.get(triggerClass);
        for (AutomationRule rule : rules) {
            if (rule.canRun(context)) {
                IActuator actuator = rule.actuator();
                Integer executionCount = runtimeAutoActuators.get(rule.name()).getSecond();
                if (executionCount != null) {
                    if (executionCount > 0){
                        actuator.execute(instance);
                        int current = executionCount - 1;
                        if (current > 0) {
                            runtimeAutoActuators.put(rule.name(), new Pair<>(rule, current));
                        } else {
                            runtimeAutoActuators.remove(rule.name());
                            contextMultimap.remove(triggerClass, rule);
                        }
                    }else if (executionCount == -1){
                        actuator.execute(instance);
                    }
                }
            }
        }
    }





    public ListTag serializeNBT() {
        ListTag listTag = new ListTag();
        for (Map.Entry<ResourceLocation, Pair<AutomationRule, Integer>> entry : runtimeAutoActuators.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", entry.getKey().toString());
            tag.putInt("executionCount", entry.getValue().getSecond());
            tag.put("rule",AutomationRule.CODEC.encodeStart(NbtOps.INSTANCE,entry.getValue().getFirst()).getOrThrow()); // 假设AutomationRule有serializeNBT方法
            listTag.add(tag);
        }
        return listTag;
    }

    public void deserializeNBT(ListTag listTag) {
        runtimeAutoActuators.clear();
        contextMultimap.clear();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            ResourceLocation ruleName = ResourceLocation.tryParse(tag.getString("name"));
            int executionCount = tag.getInt("executionCount");
            AutomationRule rule = AutomationRule.CODEC.decode(NbtOps.INSTANCE,tag.get("rule")).getOrThrow().getFirst();
            runtimeAutoActuators.put(ruleName, new Pair<>(rule, executionCount));
            rule.trigger().ifPresent(trigger -> {
                contextMultimap.put(trigger.getClass(), rule);
            });
        }
    }
}
