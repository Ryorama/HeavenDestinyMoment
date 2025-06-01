package com.xiaohunao.heaven_destiny_moment.common.init;

import com.mojang.serialization.MapCodec;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.actuator.IActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.SimpleEntitySpawnActuator;
import com.xiaohunao.heaven_destiny_moment.common.actuator.StateSettingActuator;
import com.xiaohunao.xhn_lib.api.register.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.FlexibleRegister;

public class HDMActuators {
    public static final FlexibleRegister<MapCodec<? extends IActuator>> ACTUATOR_CODEC = FlexibleRegister.create(HDMRegistries.ACTUATOR_CODEC, HeavenDestinyMoment.MODID);


    public static final FlexibleHolder<MapCodec<? extends IActuator>, MapCodec<? extends IActuator>> SIMPLE_ENTITY_SPAWN_ACTUATOR = ACTUATOR_CODEC.registerStatic("simple_entity_spawn", () -> SimpleEntitySpawnActuator.CODEC);
    public static final FlexibleHolder<MapCodec<? extends IActuator>, MapCodec<? extends IActuator>> STATE_SETTING_ACTUATOR = ACTUATOR_CODEC.registerStatic("state_setting", () -> StateSettingActuator.CODEC);
}
