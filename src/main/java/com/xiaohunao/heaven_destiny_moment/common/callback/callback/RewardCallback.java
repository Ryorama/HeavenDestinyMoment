package com.xiaohunao.heaven_destiny_moment.common.callback.callback;

import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface RewardCallback extends CallbackSerializable {
    RewardCallback EMPTY = (instance, player) -> {};

    void createReward(MomentInstance<?> instance, Player player);
}
