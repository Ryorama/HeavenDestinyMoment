package com.xiaohunao.heaven_destiny_moment.common.context.attachable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.AttributeElement;
import com.xiaohunao.heaven_destiny_moment.common.context.Weighted;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Optional;

public record CommonAttachable(Optional<Weighted<MobEffectInstance>> effects, Optional<Weighted<AttributeElement>> attributes) implements IAttachable{
    public final static MapCodec<CommonAttachable> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Weighted.codec(MobEffectInstance.CODEC).optionalFieldOf("effects").forGetter(CommonAttachable::effects),
            Weighted.codec(AttributeElement.CODEC).optionalFieldOf("attributes").forGetter(CommonAttachable::attributes)
    ).apply(instance, CommonAttachable::new));

    @Override
    public void attachToEntity(LivingEntity livingEntity) {
        effects.ifPresent(mobEffectInstances -> {
            mobEffectInstances.getRandomWeighted().forEach(livingEntity::addEffect);
        });

        attributes.ifPresent(attributeContexts -> {
            attributeContexts.getRandomWeighted().forEach(attributeElement -> {
                attributeElement.addAttribute(livingEntity);
            });
        });
    }

    @Override
    public MapCodec<? extends IAttachable> codec() {
        return CODEC;
    }

    public static class Builder implements IBuilderConverter<CommonAttachable> {
        private final Weighted.Builder<MobEffectInstance> effects = new Weighted.Builder<>();
        private final Weighted.Builder<AttributeElement> attributes = new Weighted.Builder<>();

        public CommonAttachable build(){
           return new CommonAttachable(Optional.ofNullable(effects.build()),Optional.ofNullable(attributes.build()));
        }

        public Builder effectsRandomType(Weighted.RandomType randomType){
            effects.randomType(randomType);
            return this;
        }

        public Builder attributesRandomType(Weighted.RandomType randomType){
            attributes.randomType(randomType);
            return this;
        }

        public Builder addEffect(Holder<MobEffect> effect, int duration, int amplifier){
            return addEffect(effect,duration,amplifier,1);
        }

        public Builder addEffect(Holder<MobEffect> effect, int duration, int amplifier, int weight){
            effects.add(new MobEffectInstance(effect,duration,amplifier),weight);
            return this;
        }

        public Builder addAttribute(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation){
            return addAttribute(attribute,id,amount,operation,1);
        }

        public Builder addAttribute(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation,int weight){
            attributes.add(new AttributeElement(attribute,new AttributeModifier(id,amount,operation)),weight);
            return this;
        }

        @Override
        public Builder converter(CommonAttachable commonAttachable) {
            Builder builder = new Builder();
            commonAttachable.effects().ifPresent(effects -> builder.effects.converter(effects));
            commonAttachable.attributes().ifPresent(attributes -> builder.attributes.converter(attributes));
            return builder;
        }
    }
}
