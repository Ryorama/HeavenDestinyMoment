package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.IEntityInfo;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.IEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.IReward;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.tracker.Tracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Objects;
import java.util.function.Supplier;

public class HDMRegistries {
    public static final Registry<TriggerType<?>> TRIGGER_TYPE = new RegistryBuilder<>(Keys.TRIGGER_TYPE).create();
    public static final Registry<ITrigger> TRIGGER = new RegistryBuilder<>(Keys.TRIGGER).create();
    public static final Registry<Moment> MOMENT = new RegistryBuilder<>(Keys.MOMENT).create();
    public static final Registry<MomentType<?>> MOMENT_TYPE = new RegistryBuilder<>(Keys.MOMENT_TYPE).create();

    public static final Registry<MapCodec<? extends ITracker>> TRACKER_CODEC = new RegistryBuilder<>(Keys.TRACKER_CODEC).create();
    public static final Registry<MapCodec<? extends IBarRenderType>> BAR_RENDER_TYPE_CODEC = new RegistryBuilder<>(Keys.BAR_RENDER_TYPE_CODEC).create();
    public static final Registry<MapCodec<? extends Area>> AREA_CODEC = new RegistryBuilder<>(Keys.AREA_CODEC).create();
    public static final Registry<MapCodec<? extends Moment>> MOMENT_CODEC = new RegistryBuilder<>(Keys.MOMENT_CODEC).create();
    public static final Registry<MapCodec<? extends IAmount>> AMOUNT_CODEC = new RegistryBuilder<>(Keys.AMOUNT_CODEC).create();
    public static final Registry<MapCodec<? extends ICondition>> CONDITION_CODEC = new RegistryBuilder<>(Keys.CONDITION_CODEC).create();
    public static final Registry<MapCodec<? extends IEntityInfo>> ENTITY_INFO_CODEC = new RegistryBuilder<>(Keys.ENTITY_INFO_CODEC).create();
    public static final Registry<MapCodec<? extends IReward>> REWARD_CODEC = new RegistryBuilder<>(Keys.REWARD_CODEC).create();
    public static final Registry<MapCodec<? extends IAttachable>> ATTACHABLE_CODEC = new RegistryBuilder<>(Keys.ATTACHABLE_CODEC).create();
    public static final Registry<MapCodec<? extends IEquippableSlot>> EQUIPPABLE_SLOT_CODEC = new RegistryBuilder<>(Keys.EQUIPPABLE_SLOT_CODEC).create();
    public static final Registry<MapCodec<? extends ISpawnAlgorithm>> SPAWN_ALGORITHM_CODEC = new RegistryBuilder<>(Keys.SPAWN_ALGORITHM_CODEC).create();

    public static final class Keys {
        public static final ResourceKey<Registry<ITrigger>> TRIGGER = HeavenDestinyMoment.asResourceKey("trigger");
        public static final ResourceKey<Registry<TriggerType<?>>> TRIGGER_TYPE = HeavenDestinyMoment.asResourceKey("trigger_type");
        public static final ResourceKey<Registry<MomentType<?>>> MOMENT_TYPE = HeavenDestinyMoment.asResourceKey("moment_type");
        public static final ResourceKey<Registry<MapCodec<? extends ITracker>>> TRACKER_CODEC = HeavenDestinyMoment.asResourceKey("tracker");

        public static final ResourceKey<Registry<MapCodec<? extends IBarRenderType>>> BAR_RENDER_TYPE_CODEC = HeavenDestinyMoment.asResourceKey("bar_render_type_codec");
        public static final ResourceKey<Registry<MapCodec<? extends Area>>> AREA_CODEC = HeavenDestinyMoment.asResourceKey("area_codec");
        public static final ResourceKey<Registry<MapCodec<? extends Moment>>> MOMENT_CODEC = HeavenDestinyMoment.asResourceKey("moment_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IAmount>>> AMOUNT_CODEC = HeavenDestinyMoment.asResourceKey("amount_codec");
        public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODEC = HeavenDestinyMoment.asResourceKey("condition_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IEntityInfo>>> ENTITY_INFO_CODEC = HeavenDestinyMoment.asResourceKey("entity_info_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IReward>>> REWARD_CODEC = HeavenDestinyMoment.asResourceKey("reward_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IAttachable>>> ATTACHABLE_CODEC = HeavenDestinyMoment.asResourceKey("attachable_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IEquippableSlot>>> EQUIPPABLE_SLOT_CODEC = HeavenDestinyMoment.asResourceKey("equippable_slot_codec");
        public static final ResourceKey<Registry<MapCodec<? extends ISpawnAlgorithm>>> SPAWN_ALGORITHM_CODEC = HeavenDestinyMoment.asResourceKey("spawn_algorithm_codec");


        public static final ResourceKey<Registry<Moment>> MOMENT = HeavenDestinyMoment.asResourceKey("moment");

    }

    public static final class Suppliers {
        public static final Supplier<Registry<TriggerType<?>>> TRIGGER_TYPE = supplyRegistry(Keys.TRIGGER_TYPE);
        public static final Supplier<Registry<MapCodec<? extends IBarRenderType>>> BAR_RENDER_TYPE_CODEC = supplyRegistry(Keys.BAR_RENDER_TYPE_CODEC);
        public static final Supplier<Registry<MapCodec<? extends Area>>> AREA_CODEC = supplyRegistry(Keys.AREA_CODEC);
        public static final Supplier<Registry<MapCodec<? extends Moment>>> MOMENT_CODEC = supplyRegistry(Keys.MOMENT_CODEC);
        public static final Supplier<Registry<MapCodec<? extends IAmount>>> AMOUNT_CODEC = supplyRegistry(Keys.AMOUNT_CODEC);
        public static final Supplier<Registry<MapCodec<? extends ICondition>>> CONDITION_CODEC = supplyRegistry(Keys.CONDITION_CODEC);
        public static final Supplier<Registry<MapCodec<? extends IEntityInfo>>> ENTITY_INFO_CODEC = supplyRegistry(Keys.ENTITY_INFO_CODEC);
        public static final Supplier<Registry<MapCodec<? extends IReward>>> REWARD_CODEC = supplyRegistry(Keys.REWARD_CODEC);
        public static final Supplier<Registry<MapCodec<? extends IAttachable>>> ATTACHABLE_CODEC = supplyRegistry(Keys.ATTACHABLE_CODEC);
        public static final Supplier<Registry<MapCodec<? extends IEquippableSlot>>> EQUIPPABLE_SLOT_CODEC = supplyRegistry(Keys.EQUIPPABLE_SLOT_CODEC);
        public static final Supplier<Registry<MapCodec<? extends ISpawnAlgorithm>>> SPAWN_ALGORITHM_CODEC = supplyRegistry(Keys.SPAWN_ALGORITHM_CODEC);
        public static final Supplier<Registry<MapCodec<? extends ITracker>>> TRACKER_CODEC = supplyRegistry(Keys.TRACKER_CODEC);
    }


    public static void registerRegistries(NewRegistryEvent event) {
        event.register(TRIGGER);
        event.register(TRIGGER_TYPE);
        event.register(MOMENT);
        event.register(MOMENT_TYPE);
        event.register(TRACKER_CODEC);

        event.register(BAR_RENDER_TYPE_CODEC);
        event.register(AREA_CODEC);
        event.register(MOMENT_CODEC);
        event.register(AMOUNT_CODEC);
        event.register(CONDITION_CODEC);
        event.register(ENTITY_INFO_CODEC);
        event.register(REWARD_CODEC);
        event.register(ATTACHABLE_CODEC);
        event.register(EQUIPPABLE_SLOT_CODEC);
        event.register(SPAWN_ALGORITHM_CODEC);
    }

    static <T> Supplier<T> supplyRegistry(ResourceKey<T> key) {
        return com.google.common.base.Suppliers.memoize(() -> Objects.requireNonNull((T) BuiltInRegistries.REGISTRY.get((ResourceKey) key)));
    }
}
