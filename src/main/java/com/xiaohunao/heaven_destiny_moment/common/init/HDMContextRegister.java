package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.DefaultBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.IBarRenderType;
import com.xiaohunao.heaven_destiny_moment.client.gui.bar.render.TerrariaBarRenderType;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IntegerAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.RandomAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.CommonAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.EquipmentAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.LevelCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.LocationCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.PlayerCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.AutoProbabilityCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.WorldUniqueMomentCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.DifficultyCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.TimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.*;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.IEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.VanillaEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.LocationArea;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.OpenAreaSpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.RandomPlayerPosImitationVanillaNaturalSpawner;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.tracker.MobTeamTracker;
import com.xiaohunao.heaven_destiny_moment.common.tracker.Tracker;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HDMContextRegister {

    public static final DeferredRegister<MapCodec<? extends Area>> AREA_CODEC = DeferredRegister.create(HDMRegistries.Keys.AREA_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends IAmount>> AMOUNT_CODEC = DeferredRegister.create(HDMRegistries.Keys.AMOUNT_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends IEntityInfo>> ENTITY_INFO_CODEC = DeferredRegister.create(HDMRegistries.Keys.ENTITY_INFO_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends IReward>> REWARD_CODEC = DeferredRegister.create(HDMRegistries.Keys.REWARD_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends IEquippableSlot>> EQUIPPABLE_SLOT_CODEC = DeferredRegister.create(HDMRegistries.Keys.EQUIPPABLE_SLOT_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends IAttachable>> ATTACHABLE_CODEC = DeferredRegister.create(HDMRegistries.Keys.ATTACHABLE_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends ISpawnAlgorithm>> SPAWN_ALGORITHM_CODEC = DeferredRegister.create(HDMRegistries.Keys.SPAWN_ALGORITHM_CODEC, HeavenDestinyMoment.MODID);
    public static final DeferredRegister<MapCodec<? extends ITracker>> TRACKER_CODEC = DeferredRegister.create(HDMRegistries.Keys.TRACKER_CODEC, HeavenDestinyMoment.MODID);


    public static final DeferredHolder<MapCodec<? extends Area>, MapCodec<? extends Area>> LOCATION_AREA = AREA_CODEC.register("location", () -> LocationArea.CODEC);


    public static final DeferredHolder<MapCodec<? extends IAmount>, MapCodec<? extends IAmount>> INTEGER_AMOUNT = AMOUNT_CODEC.register("integer", () -> IntegerAmount.CODEC);
    public static final DeferredHolder<MapCodec<? extends IAmount>, MapCodec<? extends IAmount>> RANDOM_AMOUNT = AMOUNT_CODEC.register("random", () -> RandomAmount.CODEC);

    public static final DeferredHolder<MapCodec<? extends IEntityInfo>, MapCodec<? extends IEntityInfo>> ENTITY_INFO = ENTITY_INFO_CODEC.register("entity_info", () -> EntityInfo.CODEC);
    public static final DeferredHolder<MapCodec<? extends IEntityInfo>, MapCodec<? extends IEntityInfo>> SLIME_INFO = ENTITY_INFO_CODEC.register("slime_info", () -> SlimeInfo.CODEC);
    public static final DeferredHolder<MapCodec<? extends IEntityInfo>, MapCodec<? extends IEntityInfo>> PIGLIN_INFO = ENTITY_INFO_CODEC.register("piglin_info", () -> PiglinInfo.CODEC);
    public static final DeferredHolder<MapCodec<? extends IEntityInfo>, MapCodec<? extends IEntityInfo>> HOGLIN_INFO = ENTITY_INFO_CODEC.register("hoglin_info", () -> HoglinInfo.CODEC);


    public static final DeferredHolder<MapCodec<? extends IReward>, MapCodec<? extends IReward>> XP_REWARD = REWARD_CODEC.register("xp", () -> XpReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends IReward>, MapCodec<? extends IReward>> EFFECT_REWARD = REWARD_CODEC.register("effect", () -> EffectReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends IReward>, MapCodec<? extends IReward>> ATTRIBUTE_REWARD = REWARD_CODEC.register("attribute", () -> AttributeReward.CODEC);
    public static final DeferredHolder<MapCodec<? extends IReward>, MapCodec<? extends IReward>> ITEM_REWARD = REWARD_CODEC.register("item", () -> ItemReward.CODEC);


    public static final DeferredHolder<MapCodec<? extends IAttachable>, MapCodec<? extends IAttachable>> COMMON_ATTACHABLE = ATTACHABLE_CODEC.register("common", () -> CommonAttachable.CODEC);
    public static final DeferredHolder<MapCodec<? extends IAttachable>, MapCodec<? extends IAttachable>> EQUIPMENT_ATTACHABLE = ATTACHABLE_CODEC.register("equipment", () -> EquipmentAttachable.CODEC);


    public static final DeferredHolder<MapCodec<? extends IEquippableSlot>, MapCodec<? extends IEquippableSlot>> VANILLA_EQUIPPABLE_SLOT = EQUIPPABLE_SLOT_CODEC.register("vanilla", () -> VanillaEquippableSlot.CODEC);


    public static final DeferredHolder<MapCodec<? extends ISpawnAlgorithm>, MapCodec<? extends ISpawnAlgorithm>> OPEN_AREA_SPAWN_ALGORITHM = SPAWN_ALGORITHM_CODEC.register("open_area", () -> OpenAreaSpawnAlgorithm.CODEC);
    public static final DeferredHolder<MapCodec<? extends ISpawnAlgorithm>, MapCodec<? extends ISpawnAlgorithm>> RANDOM_PLAYER_POS_IMITATION_VANILLA_NATURAL_SPAWNER = SPAWN_ALGORITHM_CODEC.register("random_player_pos_imitation_vanilla_natural_spawner", () -> RandomPlayerPosImitationVanillaNaturalSpawner.CODEC);


    public static final DeferredHolder<MapCodec<? extends ITracker>, MapCodec<? extends ITracker>> DEFAULT_TRACKER = TRACKER_CODEC.register("tracker", () -> Tracker.CODEC);
    public static final DeferredHolder<MapCodec<? extends ITracker>, MapCodec<? extends ITracker>> MOB_TEAM_TRACKER = TRACKER_CODEC.register("mob_team_tracker", () -> MobTeamTracker.CODEC);


    public static void register(IEventBus modEventBus) {
        AMOUNT_CODEC.register(modEventBus);
        ENTITY_INFO_CODEC.register(modEventBus);
        REWARD_CODEC.register(modEventBus);
        AREA_CODEC.register(modEventBus);
        ATTACHABLE_CODEC.register(modEventBus);
        SPAWN_ALGORITHM_CODEC.register(modEventBus);
        EQUIPPABLE_SLOT_CODEC.register(modEventBus);
        TRACKER_CODEC.register(modEventBus);
    }
}
