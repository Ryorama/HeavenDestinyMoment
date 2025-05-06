package com.xiaohunao.heaven_destiny_moment.common.context.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.callback.callback.RewardCallback;
import com.xiaohunao.heaven_destiny_moment.common.context.Weighted;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class EffectReward extends Reward {
    public static final MapCodec<EffectReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CallbackSerializable.CODEC.optionalFieldOf("rewardCallback").forGetter(EffectReward::getRewardCallback),
            Weighted.codec(MobEffectInstance.CODEC).fieldOf("effects").forGetter(EffectReward::effects)
    ).apply(instance, (callback, effects) -> (EffectReward) new EffectReward(effects).rewardCallback(callback)));

    private final Weighted<MobEffectInstance> effectInstances;

    public EffectReward(Weighted<MobEffectInstance> effectInstances) {
        this.effectInstances = effectInstances;
    }

    @Override
    public void defaultRewards(MomentInstance momentInstance, Player player) {
        effectInstances.getRandomWeighted().forEach(player::addEffect);
    }

    @Override
    public MapCodec<? extends IReward> codec() {
        return HDMContextRegister.EFFECT_REWARD.get();
    }

    public Weighted<MobEffectInstance> effects() {
        return effectInstances;
    }


    public static class Builder {
        private final Weighted.Builder<MobEffectInstance> builder = new Weighted.Builder<>();
        private RewardCallback rewardCallback;

        public EffectReward build() {
            return(EffectReward) new EffectReward(builder.build()).rewardCallback(Optional.ofNullable(rewardCallback));
        }

        public Builder randomType(Weighted.RandomType randomType) {
            builder.randomType(randomType);
            return this;
        }

        public Builder add(MobEffectInstance effectInstance, int weight) {
            builder.add(effectInstance, weight);
            return this;
        }

        public Builder add(MobEffectInstance effectInstance) {
            builder.add(effectInstance, 1);
            return this;
        }
        public Builder rewardCallback(RewardCallback rewardCallback){
            this.rewardCallback = rewardCallback;
            return this;
        }
    }
}
