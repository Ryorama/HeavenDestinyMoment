package com.xiaohunao.heaven_destiny_moment.common.moment.moment.instance;

import com.xiaohunao.heaven_destiny_moment.common.init.HDMMomentTypes;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DefaultInstance extends MomentInstance {
    public DefaultInstance(Level level, Moment moment) {
        super(HDMMomentTypes.DEFAULT.get(),level, moment);
    }

    public DefaultInstance(UUID uuid, Level level, Moment moment) {
        super(HDMMomentTypes.DEFAULT.get(),uuid, level, moment);
    }


}
