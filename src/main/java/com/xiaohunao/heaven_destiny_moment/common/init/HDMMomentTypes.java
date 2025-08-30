package com.xiaohunao.heaven_destiny_moment.common.init;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentType;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.DefaultInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance.RaidInstance;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import com.xiaohunao.xhn_lib.api.register.register.FlexibleRegister;

public class HDMMomentTypes {
    public static final FlexibleRegister<MomentType<?>> MOMENT_TYPE = FlexibleRegister.create(HDMRegistries.MOMENT_TYPE, HeavenDestinyMoment.MODID);

    public static final FlexibleHolder<MomentType<?>,?> DEFAULT = MOMENT_TYPE.registerStatic("default",
            () -> new MomentType.Builder<>(DefaultInstance::new).build());

    public static final FlexibleHolder<MomentType<?>,?> RAID = MOMENT_TYPE.registerStatic("raid",
            () -> new MomentType.Builder<>(RaidInstance::new).build());


}
