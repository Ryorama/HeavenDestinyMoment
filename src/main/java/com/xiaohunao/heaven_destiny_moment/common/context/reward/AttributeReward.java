package com.xiaohunao.heaven_destiny_moment.common.context.reward;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.callback.callback.RewardCallback;
import com.xiaohunao.heaven_destiny_moment.common.context.AttributeElement;
import com.xiaohunao.heaven_destiny_moment.common.context.Weighted;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;


public class AttributeReward extends Reward {
    public static final ResourceLocation ID = HeavenDestinyMoment.asResource("attribute");
    public static final MapCodec<AttributeReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CallbackSerializable.CODEC.optionalFieldOf("rewardCallback").forGetter(AttributeReward::getRewardCallback),
            Weighted.codec(AttributeElement.CODEC).fieldOf("attributes").forGetter(AttributeReward::attributes)
    ).apply(instance, (callback, attributes) -> (AttributeReward) new AttributeReward(attributes).rewardCallback(callback)));


    private final Weighted<AttributeElement> attributes;

    public AttributeReward(Weighted<AttributeElement> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void defaultRewards(MomentInstance<?> momentInstance, Player player) {
        attributes.getRandomWeighted().forEach(reward -> {
            AttributeModifier attributeModifier = reward.attributeModifier();
            AttributeInstance instance = player.getAttribute(reward.attribute());
            if (instance != null) {
                instance.removeModifier(attributeModifier);
                instance.addPermanentModifier(attributeModifier);
            }
        });
    }

    @Override
    public MapCodec<? extends IReward> codec() {
        return HDMContextRegister.ATTRIBUTE_REWARD.get();
    }

    public Weighted<AttributeElement> attributes() {
        return attributes;
    }

    public static class Builder {
        private final Weighted.Builder<AttributeElement> builder = new Weighted.Builder<>();
        private RewardCallback rewardCallback;

        public AttributeReward build() {
            return (AttributeReward) new AttributeReward(builder.build()).rewardCallback(Optional.ofNullable(rewardCallback));
        }

        public Builder randomType(Weighted.RandomType randomType) {
            builder.randomType(randomType);
            return this;
        }

        public Builder add(AttributeElement element) {
            builder.add(element, 1);
            return this;
        }

        public Builder add(AttributeElement element, int weight) {
            builder.add(element, weight);
            return this;
        }

        public Builder add(Holder<Attribute> attribute, AttributeModifier attributeModifier) {
            builder.add(new AttributeElement(attribute, attributeModifier), 1);
            return this;
        }

        public Builder add(Holder<Attribute> attribute, AttributeModifier attributeModifier, int weight) {
            builder.add(new AttributeElement(attribute, attributeModifier), weight);
            return this;
        }
        public Builder rewardCallback(RewardCallback rewardCallback){
            this.rewardCallback = rewardCallback;
            return this;
        }
    }


}
