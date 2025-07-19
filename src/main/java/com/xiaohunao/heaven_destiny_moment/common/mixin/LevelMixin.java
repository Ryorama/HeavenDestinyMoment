package com.xiaohunao.heaven_destiny_moment.common.mixin;


import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin implements MomentManagerMixed {
    @Unique
    private MomentInstanceManager heaven_destiny_moment$momentInstanceManager;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void heaven_destiny_moment$init(CallbackInfo ci) {
        heaven_destiny_moment$momentInstanceManager = new MomentInstanceManager((Level) (Object) this);
    }


    public MomentInstanceManager heaven_destiny_moment$getMomentManager() {
        return heaven_destiny_moment$momentInstanceManager;
    }

}
