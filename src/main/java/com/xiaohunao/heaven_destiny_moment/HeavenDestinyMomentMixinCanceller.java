package com.xiaohunao.heaven_destiny_moment;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class HeavenDestinyMomentMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        HeavenDestinyMoment.LOGGER.info("Running Mixin Canceller");
        if (mixinClassName.equals("dev.corgitaco.enhancedcelestials.mixin.client.MixinWorldRenderer")) {
            HeavenDestinyMoment.LOGGER.info("Cancelling EC Mixin");
            return true;
        }
        return false;
    }
}
