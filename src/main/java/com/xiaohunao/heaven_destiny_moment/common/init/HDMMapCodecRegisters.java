package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.actuator.CreateMomentInstanceActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.SimpleEntitySpawnActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IntegerAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.RandomAmount;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.CommonAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.EquipmentAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.attachable.IAttachable;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.AutoActuatorCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.*;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.DifficultyCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.LevelCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.LevelRunningTimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.level.TimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.moment.MomentHistoryCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.moment.MomentRunningTimeCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.player.PlayerCondition;
import com.xiaohunao.heaven_destiny_moment.common.context.entity_info.*;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.IEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.context.equippable_slot.VanillaEquippableSlot;
import com.xiaohunao.heaven_destiny_moment.common.context.reward.*;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.Area;
import com.xiaohunao.heaven_destiny_moment.common.moment.area.LocationArea;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.DefaultMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.RaidMoment;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.ISpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.OpenAreaSpawnAlgorithm;
import com.xiaohunao.heaven_destiny_moment.common.spawn_algorithm.RandomPlayerPosImitationVanillaNaturalSpawner;
import com.xiaohunao.heaven_destiny_moment.common.tracker.ITracker;
import com.xiaohunao.heaven_destiny_moment.common.tracker.MobTeamTracker;
import com.xiaohunao.heaven_destiny_moment.common.tracker.Tracker;
import com.xiaohunao.heaven_destiny_moment.common.trigger.ITrigger;
import com.xiaohunao.heaven_destiny_moment.common.trigger.triggers.*;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.MapCodecFlexibleRegister;
import net.neoforged.bus.api.IEventBus;

public class HDMMapCodecRegisters {
    public static final MapCodecFlexibleRegister<IMoment> MOMENT_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.MOMENT_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "default", DefaultMoment.CODEC,
                    "raid", RaidMoment.CODEC
            );

    public static final MapCodecFlexibleRegister<ICondition> CONDITION_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.CONDITION_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "time", TimeCondition.CODEC,
                    "location",LocationCondition.CODEC,
                    "world_unique_moment", WorldUniqueMomentCondition.CODEC,
                    "difficulty", DifficultyCondition.CODEC,
                    "level", LevelCondition.CODEC,
                    "kill_entity", KillEntityCondition.CODEC,
                    "player", PlayerCondition.CODEC,
                    "mod_loaded", ModLoadedCondition.CODEC,
                    "or", OrCondition.CODEC,
                    "level_running_time", LevelRunningTimeCondition.CODEC,
                    "moment_history_time", MomentHistoryCondition.CODEC,
                    "moment_running_time", MomentRunningTimeCondition.CODEC,
                    "invert", InvertCondition.CODEC,
                    "list", ListCondition.CODEC,
                    "auto_actuator", AutoActuatorCondition.CODEC
            );

    public static final MapCodecFlexibleRegister<IAmount> AMOUNT_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.AMOUNT_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "integer", IntegerAmount.CODEC,
                    "random", RandomAmount.CODEC
            );


    public static final MapCodecFlexibleRegister<IReward> REWARD_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.REWARD_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "xp", XpReward.CODEC,
                    "effect", EffectReward.CODEC,
                    "attribute", AttributeReward.CODEC,
                    "item", ItemReward.CODEC
            );

    public static final MapCodecFlexibleRegister<IEntityInfo> ENTITY_INFO_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.ENTITY_INFO_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "entity_info", EntityInfo.CODEC,
                    "slime_info", SlimeInfo.CODEC,
                    "piglin_info", PiglinInfo.CODEC,
                    "hoglin_info", HoglinInfo.CODEC
            );

    public static final MapCodecFlexibleRegister<Area> AREA_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.AREA_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "location_area", LocationArea.CODEC
            );

    public static final MapCodecFlexibleRegister<ISpawnAlgorithm> SPAWN_ALGORITHM_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.SPAWN_ALGORITHM_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "open_area", OpenAreaSpawnAlgorithm.CODEC,
                    "random_player_pos_imitation_vanilla_natural_spawner", RandomPlayerPosImitationVanillaNaturalSpawner.CODEC
            );

    public static final MapCodecFlexibleRegister<ITracker> TRACKER_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.TRACKER_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "tracker", Tracker.CODEC,
                    "mob_team_tracker", MobTeamTracker.CODEC
            );

    public static final MapCodecFlexibleRegister<IEquippableSlot> EQUIPPABLE_SLOT_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.EQUIPPABLE_SLOT_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "vanilla", VanillaEquippableSlot.CODEC
            );

    public static final MapCodecFlexibleRegister<IAttachable> ATTACHABLE_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.ATTACHABLE_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "common", CommonAttachable.CODEC,
                    "equipment", EquipmentAttachable.CODEC
            );

    public static final MapCodecFlexibleRegister<IActuator> ACTUATOR_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.ACTUATOR_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "simple_entity_spawn", SimpleEntitySpawnActuator.CODEC,
                    "state_setting", StateSettingActuator.CODEC,
                    "create_moment_instance", CreateMomentInstanceActuator.CODEC
            );


    public static final MapCodecFlexibleRegister<ITrigger> TRIGGER_CODEC = MapCodecFlexibleRegister.createMapCodec(HDMRegistries.Keys.TRIGGER_CODEC, HeavenDestinyMoment.MODID)
            .addMapCodec(
                    "level_tick", LevelTickTrigger.CODEC,
                    "random_level_tick", RandomLevelTickTrigger.CODEC,
                    "time_probability", TimeProbabilityTrigger.CODEC,
                    "kill_entity", KillEntityTrigger.CODEC,
                    "conditional", ConditionalTrigger.CODEC,
                    "block_break", BlockBreakTrigger.CODEC
            );

    public static void register(IEventBus modEventBus) {
        MOMENT_CODEC.register(modEventBus);
        CONDITION_CODEC.register(modEventBus);
        AMOUNT_CODEC.register(modEventBus);
        REWARD_CODEC.register(modEventBus);
        AREA_CODEC.register(modEventBus);
        ENTITY_INFO_CODEC.register(modEventBus);
        SPAWN_ALGORITHM_CODEC.register(modEventBus);
        TRACKER_CODEC.register(modEventBus);
        EQUIPPABLE_SLOT_CODEC.register(modEventBus);
        ATTACHABLE_CODEC.register(modEventBus);
        ACTUATOR_CODEC.register(modEventBus);
        TRIGGER_CODEC.register(modEventBus);
    }
}
