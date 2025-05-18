package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ConditionalTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerContext;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;

import java.util.List;
import java.util.Map;

public record StateSettingsGroup(Map<MomentState, TriggerContext> states) {
    public static final Codec<StateSettingsGroup> CODEC = Codec.unboundedMap(MomentState.CODEC, TriggerContext.CODEC).xmap(StateSettingsGroup::new, StateSettingsGroup::states);


    public static class Builder {
        private final Map<MomentState, TriggerContext> autoStates = Maps.newHashMap();

        public StateSettingsGroup build() {
            return new StateSettingsGroup(autoStates);
        }

        public Builder state(MomentState state, TriggerContext... hook) {
            if (state != null && hook != null) {
                for (TriggerContext triggerContext : hook) {
                    autoStates.put(state, triggerContext);
                }
            }
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger, ICondition... conditions) {
            if (state != null && trigger != null && conditions != null) {
                autoStates.put(state, TriggerContext.of(trigger, conditions));
            }
            return this;
        }

        public Builder state(MomentState state,ICondition... conditions) {
            if (state != null && conditions != null) {
                autoStates.put(state, TriggerContext.of(new ConditionalTrigger(List.of(conditions))));
            }
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger) {
            if (state != null && trigger != null) {
                autoStates.put(state, TriggerContext.of(trigger));
            }
            return this;
        }

    }
}
