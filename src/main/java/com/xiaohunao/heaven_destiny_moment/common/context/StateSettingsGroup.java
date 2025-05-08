package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
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
import java.util.Optional;

public record StateSettingsGroup(Optional<List<ConditionalTrigger>> creates, Optional<Multimap<MomentState, ConditionalTrigger>> states) {

    public static final Codec<StateSettingsGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConditionalTrigger.CODEC.listOf().optionalFieldOf("creates").forGetter(StateSettingsGroup::creates),
            CodecUtils.multimapCodec(MomentState.CODEC, ConditionalTrigger.CODEC).optionalFieldOf("autoStates").forGetter(StateSettingsGroup::states)
    ).apply(instance, StateSettingsGroup::new));


    public boolean matchCreate(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        if (creates.isEmpty()) {
            return true;
        }

        List<ConditionalTrigger> triggerList = creates.get();
        if (triggerList.isEmpty()) {
            return true;
        }

        boolean foundTrigger = false;
        boolean allConditionsMatch = false;

        for (ConditionalTrigger conditionalTrigger : triggerList) {
            if (conditionalTrigger.trigger().canTrigger(instance, pos, serverPlayer)) {
                foundTrigger = true;

                for (ICondition condition : conditionalTrigger.conditions()) {
                    if (condition.matches(instance, pos, serverPlayer)) {
                        allConditionsMatch = true;
                    }else {
                        allConditionsMatch = false;
                        break;
                    }
                }
            }
        }

        return foundTrigger && allConditionsMatch;
    }


    public static class Builder {
        private final List<ConditionalTrigger> autoCreates = Lists.newArrayList();
        private final Multimap<MomentState, ConditionalTrigger> autoStates = HashMultimap.create();

        public StateSettingsGroup build() {
            return new StateSettingsGroup(
                Optional.of(autoCreates),
                Optional.of(autoStates)
            );
        }

        public Builder create(ConditionalTrigger... autoCreate) {
            if (autoCreate != null) {
                autoCreates.addAll(List.of(autoCreate));
            }
            return this;
        }

        public Builder create(ITrigger trigger, ICondition... conditions) {
            if (trigger != null && conditions != null) {
                autoCreates.add(ConditionalTrigger.of(trigger, conditions));
            }
            return this;
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
