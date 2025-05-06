package com.xiaohunao.heaven_destiny_moment.common.mixin.client;

import com.xiaohunao.heaven_destiny_moment.common.mixed.ClientMomentInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ClientMomentInstanceMixed {
    @Unique
    private MomentInstance heaven_destiny_moment$momentInstance;

    @Override
    public MomentInstance heaven_destiny_moment$getClientMomentInstance() {
        return heaven_destiny_moment$momentInstance;
    }

    @Override
    public void heaven_destiny_moment$setClientMomentInstance(MomentInstance momentInstance) {
        this.heaven_destiny_moment$momentInstance = momentInstance;
    }
}
