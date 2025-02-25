package com.xiaohunao.heaven_destiny_moment.common.context.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.callback.callback.RewardCallback;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class XpReward extends Reward {
    public static final MapCodec<XpReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CallbackSerializable.CODEC.optionalFieldOf("rewardCallback").forGetter(XpReward::getRewardCallback),
            Codec.INT.fieldOf("xp").forGetter(XpReward::getXp)
    ).apply(instance, (callback, xp) -> (XpReward) new XpReward(xp).rewardCallback(callback)));
    private final int xp;

    public XpReward(int xp) {
        this.xp = xp;
    }

    @Override
    public void defaultRewards(MomentInstance<?> moment, Player player) {
        player.giveExperiencePoints(xp);
    }

    public int getXp() {
        return xp;
    }

    @Override
    public MapCodec<? extends IReward> codec() {
        return HDMContextRegister.XP_REWARD.get();
    }

    public int xp() {
        return xp;
    }

    public static class Builder {
        private final int xp;
        private RewardCallback rewardCallback;

        public Builder(int xp) {
            this.xp = xp;
        }

        public XpReward build() {
            return (XpReward) new XpReward(xp).rewardCallback(Optional.ofNullable(rewardCallback));
        }

        public Builder rewardCallback(RewardCallback rewardCallback){
            this.rewardCallback = rewardCallback;
            return this;
        }
    }
}
