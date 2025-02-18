package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Map;
import java.util.Optional;

public record TipSettings(Optional<Map<MomentState, Holder<SoundEvent>>> soundEvents, Optional<Map<MomentState, Component>> texts) {
    public static final TipSettings EMPTY = new TipSettings(Optional.empty(),Optional.empty());

    public static final Codec<TipSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(MomentState.CODEC, SoundEvent.CODEC).optionalFieldOf("soundEvents").forGetter(TipSettings::soundEvents),
            Codec.unboundedMap(MomentState.CODEC, ComponentSerialization.CODEC).optionalFieldOf("texts").forGetter(TipSettings::texts)
    ).apply(instance, TipSettings::new));

    public void playTooltip(MomentInstance<?> instance) {
        if (!instance.getLevel().isClientSide) return;

        texts.ifPresent(texts ->{
            Component component = texts.getOrDefault(instance.getState(), null);
            if (component != null) {
                instance.getPlayers().forEach(player -> player.sendSystemMessage(component));
            }
        });

        soundEvents.ifPresent(soundEvents -> {
            Holder<SoundEvent> soundEvent = soundEvents.getOrDefault(instance.getState(), null);
            if (soundEvent != null) {
                instance.getPlayers().forEach(player ->
                        instance.getLevel().playSound(player, player.blockPosition(), soundEvent.value(), SoundSource.MASTER, 1.0F, 1.0F));
            }
        });
    }


    public static class Builder {
        private Map<MomentState, Component> texts;
        private Map<MomentState, Holder<SoundEvent>> soundEvents;

        public Builder tooltip(MomentState momentState, String descriptionId) {
            return tooltip(momentState,Component.translatable("moment.tooltip.text." + momentState.getSerializedName() + "." + descriptionId));
        }

        public Builder tooltip(MomentState momentState, String descriptionId, int color) {
            return tooltip(momentState,Component.translatable("moment.tooltip.text." + momentState.getSerializedName() + "." + descriptionId),color);
        }

        public Builder tooltip(MomentState momentState, Component component) {
            if (texts == null){
                texts = Maps.newHashMap();
            }
            texts.put(momentState,component);
            return this;
        }

        public Builder tooltip(MomentState momentState, Component component,int color) {
            if (texts == null){
                texts = Maps.newHashMap();
            }
            texts.put(momentState,component.copy().withStyle(style -> style.withColor(color)));
            return this;
        }

        public Builder tooltip(MomentState momentState, Holder<SoundEvent> soundEvent) {
            if (soundEvents == null){
                soundEvents = Maps.newHashMap();
            }
            soundEvents.put(momentState,soundEvent);
            return this;
        }

        public TipSettings build() {
            return new TipSettings(Optional.ofNullable(soundEvents), Optional.ofNullable(texts));
        }

    }
}
