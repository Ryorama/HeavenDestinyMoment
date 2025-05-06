package com.xiaohunao.heaven_destiny_moment.api;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.xhn_lib.api.data.loader.SimpleDynamicLoader;
import com.xiaohunao.xhn_lib.common.serialization.DynamicSerializerType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class MomentManager extends SimpleDynamicLoader<Moment> {
    private static final MomentManager INSTANCE = new MomentManager();
    private static final String FOLDER = "heaven_destiny_moment/moment";

    private MomentManager() {
        super(FOLDER, HDMRegistries.MOMENT, DynamicSerializerType.of(IMoment.CODEC));
    }

    public static MomentManager getInstance(){
        return INSTANCE;
    }



    @Override
    public void loadValue(ResourceLocation location, Moment value) {

    }
}
