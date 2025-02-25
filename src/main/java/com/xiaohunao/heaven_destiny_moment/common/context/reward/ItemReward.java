package com.xiaohunao.heaven_destiny_moment.common.context.reward;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.callback.CallbackSerializable;
import com.xiaohunao.heaven_destiny_moment.common.callback.callback.RewardCallback;
import com.xiaohunao.heaven_destiny_moment.common.context.Weighted;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemReward extends Reward {
    public static final MapCodec<ItemReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CallbackSerializable.CODEC.optionalFieldOf("rewardCallback").forGetter(ItemReward::getRewardCallback),
        Weighted.codec(ItemStack.CODEC).fieldOf("items").forGetter(ItemReward::items)
    ).apply(instance, (callback, items) -> (ItemReward) new ItemReward(items).rewardCallback(callback)));

    private final Weighted<ItemStack> items;



    public ItemReward(Weighted<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void defaultRewards(MomentInstance<?> moment, Player player) {
        items.getRandomWeighted().forEach(item -> {
            player.getInventory().add(item);
        });
    }

    @Override
    public MapCodec<? extends IReward> codec() {
        return HDMContextRegister.ITEM_REWARD.get();
    }

    public Weighted<ItemStack> items() {
        return items;
    }

    public static class Builder {
        private final Weighted.Builder<ItemStack> builder = new Weighted.Builder<>();
        private RewardCallback rewardCallback;

        public Builder randomType(Weighted.RandomType randomType) {
            builder.randomType(randomType);
            return this;
        }

        public ItemReward build() {
            return (ItemReward)new ItemReward(builder.build()).rewardCallback(Optional.ofNullable(rewardCallback));
        }

        public Builder add(ItemStack itemStack, int weight) {
            builder.add(itemStack, weight);
            return this;
        }

        public Builder add(ItemStack itemStack) {
            builder.add(itemStack, 1);
            return this;
        }

        public Builder add(Item item, int weight) {
            builder.add(item.getDefaultInstance(), weight);
            return this;
        }

        public Builder add(Item item) {
            builder.add(item.getDefaultInstance(), 1);
            return this;
        }

        public Builder rewardCallback(RewardCallback rewardCallback){
            this.rewardCallback = rewardCallback;
            return this;
        }


    }

}
