package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
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
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.TriggerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class HDMRegistries {
    public static final Registry<Moment> MOMENT = new RegistryBuilder<>(Keys.MOMENT).create();
    public static final Registry<MomentType<?>> MOMENT_TYPE = new RegistryBuilder<>(Keys.MOMENT_TYPE).create();
    public static final Registry<IBarRenderType> BAR_RENDER_TYPE = new RegistryBuilder<>(Keys.BAR_RENDER_TYPE).create();
    public static final Registry<BiFunction<Integer, Difficulty, Integer>> DIFFICULTY_SCALING = new RegistryBuilder<>(Keys.DIFFICULTY_SCALING).create();
    public static final Registry<BiFunction<Integer, Integer, Integer>> PLAYER_COUNT_SCALING = new RegistryBuilder<>(Keys.PLAYER_COUNT_SCALING).create();
    public static final Registry<TriggerType<?>> TRIGGER_TYPE = new RegistryBuilder<>(Keys.TRIGGER_TYPE).create();


    public static final Registry<MapCodec<? extends IActuator>> ACTUATOR_CODEC = new RegistryBuilder<>(Keys.ACTUATOR_CODEC).create();
    public static final Registry<MapCodec<? extends ITracker>> TRACKER_CODEC = new RegistryBuilder<>(Keys.TRACKER_CODEC).create();
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
        public static final ResourceKey<Registry<BiFunction<Integer, Difficulty, Integer>>> DIFFICULTY_SCALING = HeavenDestinyMoment.asResourceKey("difficulty_scaling");
        public static final ResourceKey<Registry<BiFunction<Integer, Integer, Integer>>> PLAYER_COUNT_SCALING = HeavenDestinyMoment.asResourceKey("player_count_scaling");
        public static final ResourceKey<Registry<MomentType<?>>> MOMENT_TYPE = HeavenDestinyMoment.asResourceKey("moment_type");
        public static final ResourceKey<Registry<TriggerType<?>>> TRIGGER_TYPE = HeavenDestinyMoment.asResourceKey("trigger_type");
        public static final ResourceKey<Registry<MapCodec<? extends ITracker>>> TRACKER_CODEC = HeavenDestinyMoment.asResourceKey("tracker");
        public static final ResourceKey<Registry<IBarRenderType>> BAR_RENDER_TYPE = HeavenDestinyMoment.asResourceKey("bar_render_type");


        public static final ResourceKey<Registry<MapCodec<? extends IActuator>>> ACTUATOR_CODEC = HeavenDestinyMoment.asResourceKey("actuator_codec");
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


    public static void registerRegistries(NewRegistryEvent event) {
        event.register(MOMENT);
        event.register(TRIGGER_TYPE);
        event.register(MOMENT_TYPE);
        event.register(TRACKER_CODEC);
        event.register(BAR_RENDER_TYPE);
        event.register(DIFFICULTY_SCALING);
        event.register(PLAYER_COUNT_SCALING);

        event.register(ACTUATOR_CODEC);
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
