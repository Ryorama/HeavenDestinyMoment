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
import com.xiaohunao.heaven_destiny_moment.common.function.MomentKillEntityConditionDifficultyScalingFunction;
import com.xiaohunao.heaven_destiny_moment.common.function.MomentProbabilityFunction;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Objects;
import java.util.function.Supplier;

public class HDMRegistries {
    public static final Registry<IMoment> MOMENT = new RegistryBuilder<>(Keys.MOMENT).create();
    public static final Registry<MomentType<?>> MOMENT_TYPE = new RegistryBuilder<>(Keys.MOMENT_TYPE).create();
    public static final Registry<IBarRenderType> BAR_RENDER_TYPE = new RegistryBuilder<>(Keys.BAR_RENDER_TYPE).create();
    public static final Registry<MomentKillEntityConditionDifficultyScalingFunction> MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION = new RegistryBuilder<>(Keys.MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION).create();
    public static final Registry<MomentProbabilityFunction> MOMENT_PROBABILITY_FUNCTION = new RegistryBuilder<>(Keys.MOMENT_PROBABILITY_FUNCTION).create();

    public static final Registry<MapCodec<? extends IMoment>> MOMENT_CODEC = new RegistryBuilder<>(Keys.MOMENT_CODEC).create();

    public static final Registry<MapCodec<? extends IActuator>> ACTUATOR_CODEC = new RegistryBuilder<>(Keys.ACTUATOR_CODEC).create();
    public static final Registry<MapCodec<? extends ITracker>> TRACKER_CODEC = new RegistryBuilder<>(Keys.TRACKER_CODEC).create();
    public static final Registry<MapCodec<? extends Area>> AREA_CODEC = new RegistryBuilder<>(Keys.AREA_CODEC).create();
    public static final Registry<MapCodec<? extends IAmount>> AMOUNT_CODEC = new RegistryBuilder<>(Keys.AMOUNT_CODEC).create();
    public static final Registry<MapCodec<? extends ICondition>> CONDITION_CODEC = new RegistryBuilder<>(Keys.CONDITION_CODEC).create();
    public static final Registry<MapCodec<? extends IEntityInfo>> ENTITY_INFO_CODEC = new RegistryBuilder<>(Keys.ENTITY_INFO_CODEC).create();
    public static final Registry<MapCodec<? extends IReward>> REWARD_CODEC = new RegistryBuilder<>(Keys.REWARD_CODEC).create();
    public static final Registry<MapCodec<? extends IAttachable>> ATTACHABLE_CODEC = new RegistryBuilder<>(Keys.ATTACHABLE_CODEC).create();
    public static final Registry<MapCodec<? extends IEquippableSlot>> EQUIPPABLE_SLOT_CODEC = new RegistryBuilder<>(Keys.EQUIPPABLE_SLOT_CODEC).create();
    public static final Registry<MapCodec<? extends ISpawnAlgorithm>> SPAWN_ALGORITHM_CODEC = new RegistryBuilder<>(Keys.SPAWN_ALGORITHM_CODEC).create();
    public static final Registry<MapCodec<? extends ITrigger>> TRIGGER_CODEC = new RegistryBuilder<>(Keys.TRIGGER_CODEC).create();


    public static final class Keys {
        public static final ResourceKey<Registry<MomentKillEntityConditionDifficultyScalingFunction>> MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION = HeavenDestinyMoment.asResourceKey("moment_kill_entity_condition_difficulty_scaling_function");
        public static final ResourceKey<Registry<MomentProbabilityFunction>> MOMENT_PROBABILITY_FUNCTION = HeavenDestinyMoment.asResourceKey("moment_probability_function");

        public static final ResourceKey<Registry<MomentType<?>>> MOMENT_TYPE = HeavenDestinyMoment.asResourceKey("moment_type");
        public static final ResourceKey<Registry<MapCodec<? extends ITracker>>> TRACKER_CODEC = HeavenDestinyMoment.asResourceKey("tracker");
        public static final ResourceKey<Registry<IBarRenderType>> BAR_RENDER_TYPE = HeavenDestinyMoment.asResourceKey("bar_render_type");


        public static final ResourceKey<Registry<MapCodec<? extends IActuator>>> ACTUATOR_CODEC = HeavenDestinyMoment.asResourceKey("actuator_codec");
        public static final ResourceKey<Registry<MapCodec<? extends Area>>> AREA_CODEC = HeavenDestinyMoment.asResourceKey("area_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IMoment>>> MOMENT_CODEC = HeavenDestinyMoment.asResourceKey("moment_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IAmount>>> AMOUNT_CODEC = HeavenDestinyMoment.asResourceKey("amount_codec");
        public static final ResourceKey<Registry<MapCodec<? extends ICondition>>> CONDITION_CODEC = HeavenDestinyMoment.asResourceKey("condition_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IEntityInfo>>> ENTITY_INFO_CODEC = HeavenDestinyMoment.asResourceKey("entity_info_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IReward>>> REWARD_CODEC = HeavenDestinyMoment.asResourceKey("reward_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IAttachable>>> ATTACHABLE_CODEC = HeavenDestinyMoment.asResourceKey("attachable_codec");
        public static final ResourceKey<Registry<MapCodec<? extends IEquippableSlot>>> EQUIPPABLE_SLOT_CODEC = HeavenDestinyMoment.asResourceKey("equippable_slot_codec");
        public static final ResourceKey<Registry<MapCodec<? extends ISpawnAlgorithm>>> SPAWN_ALGORITHM_CODEC = HeavenDestinyMoment.asResourceKey("spawn_algorithm_codec");
        public static final ResourceKey<Registry<MapCodec<? extends ITrigger>>> TRIGGER_CODEC = HeavenDestinyMoment.asResourceKey("trigger_codec");

        public static final ResourceKey<Registry<IMoment>> MOMENT = HeavenDestinyMoment.asResourceKey("moment");
    }


    public static void registerRegistries(NewRegistryEvent event) {
        event.register(MOMENT);
        event.register(MOMENT_TYPE);
        event.register(BAR_RENDER_TYPE);
        event.register(MOMENT_KILL_ENTITY_CONDITION_DIFFICULTY_SCALING_FUNCTION);
        event.register(MOMENT_PROBABILITY_FUNCTION);


        event.register(MOMENT_CODEC);
        event.register(TRACKER_CODEC);
        event.register(AREA_CODEC);
        event.register(EQUIPPABLE_SLOT_CODEC);
        event.register(AMOUNT_CODEC);
        event.register(ENTITY_INFO_CODEC);
        event.register(REWARD_CODEC);
        event.register(SPAWN_ALGORITHM_CODEC);
        event.register(TRIGGER_CODEC);
        event.register(CONDITION_CODEC);
        event.register(ACTUATOR_CODEC);
        event.register(ATTACHABLE_CODEC);
    }

    static <T> Supplier<T> supplyRegistry(ResourceKey<T> key) {
        return com.google.common.base.Suppliers.memoize(() -> Objects.requireNonNull((T) BuiltInRegistries.REGISTRY.get((ResourceKey) key)));
    }
}
