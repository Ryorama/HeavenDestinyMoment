package com.xiaohunao.heaven_destiny_moment.common.context.reward;

import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.callback.callback.RewardCallback;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public abstract class Reward implements IReward{
    protected Optional<RewardCallback> rewardCallback;

    @Override
    public void createReward(MomentInstance momentInstance, Player player) {
        rewardCallback.ifPresentOrElse(
                callback -> callback.createReward(momentInstance, player),
                () -> defaultRewards(momentInstance, player)
        );
    }


    public abstract void defaultRewards(MomentInstance momentInstance, Player player);

    public Optional<CallbackSerializable> getRewardCallback() {
        if (rewardCallback.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(rewardCallback.get());
    }
    public Reward rewardCallback(Optional<CallbackSerializable> rewardCallback) {
        if (rewardCallback.isEmpty()){
            this.rewardCallback = Optional.empty();
            return this;
        }
        this.rewardCallback = Optional.of((RewardCallback)rewardCallback.get());
        return this;
    }
}
