package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ConditionalTrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record StateSettingsGroup(Map<MomentState, ConditionalTrigger> states) {
    public static final Codec<StateSettingsGroup> CODEC = Codec.unboundedMap(MomentState.CODEC, ConditionalTrigger.CODEC).xmap(StateSettingsGroup::new, StateSettingsGroup::states);


    public static class Builder {
        private final Map<MomentState, ConditionalTrigger> autoStates = Maps.newHashMap();

        public StateSettingsGroup build() {
            return new StateSettingsGroup(autoStates);
        }

        public Builder state(MomentState state, ConditionalTrigger... hook) {
            if (state != null && hook != null) {
                for (ConditionalTrigger conditionalTrigger : hook) {
                    autoStates.put(state, conditionalTrigger);
                }
            }
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger, ICondition... conditions) {
            if (state != null && trigger != null && conditions != null) {
                autoStates.put(state, ConditionalTrigger.of(trigger, conditions));
            }
            return this;
        }

        public Builder state(MomentState state, ITrigger trigger) {
            if (state != null && trigger != null) {
                autoStates.put(state, ConditionalTrigger.of(trigger));
            }
            return this;
        }

    }
}
